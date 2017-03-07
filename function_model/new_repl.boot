

(def log-config
  [:configuration {:scan true, :scanPeriod "10 seconds"}
   [:appender {:name "FILE" :class "ch.qos.logback.core.rolling.RollingFileAppender"}
    [:encoder [:pattern "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"]]
    [:rollingPolicy {:class "ch.qos.logback.core.rolling.TimeBasedRollingPolicy"}
     [:fileNamePattern "logs/%d{yyyy-MM-dd}.%i.log"]
     [:timeBasedFileNamingAndTriggeringPolicy 
        {:class "ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP"}
        [:maxFileSize "64 MB"]]]
    [:prudent true]]
   [:appender {:name "STDOUT" :class "ch.qos.logback.core.ConsoleAppender"}
    [:encoder [:pattern "%-5level %logger{36} - %msg%n"]]
    [:filter {:class "ch.qos.logback.classic.filter.ThresholdFilter"}
     [:level "INFO"]]]
   [:root {:level "INFO"}
    [:appender-ref {:ref "FILE"}]]
    ;; [:appender-ref {:ref "STDOUT"}]]
   [:logger {:name "user" :level "INFO"}]
   [:logger {:name "boot.user" :level "INFO"}]])

(set-env!
 :resource-paths #{"res"}
 :source-paths #{"src/clj"}
 :asset-paths #{"asset"}
 :dependencies
   '[[cheshire "5.7.0"]
     [juleswhite/clj-cure "0.1.0"]
     [adzerk/bootlaces "0.1.13"]
     [org.clojure/tools.logging "0.3.1"]
     [adzerk/boot-logservice "1.2.0"]
     [ch.qos.logback/logback-classic "1.1.7"]
     [mvxcvi/puget "1.0.1"]
     [reply "0.3.7"]])

(require '[adzerk.boot-logservice :as log-service]
         '[clojure.tools.logging  :as log]
         '[puget.printer :refer [cprint]])

(require '[boot.pod             :as pod]
         '[boot.core            :as core]
         '[boot.util            :as util]
         '[boot.repl            :as repl])

(require '[boot.git :refer [last-commit]]
         '[adzerk.bootlaces :refer [bootlaces! build-jar]]
         '[immortals.function-model :as fm]
         '[cure.core :as cure]
         '[cheshire.core :as json])

(alter-var-root #'log/*logger-factory* 
                (constantly (log-service/make-factory log-config)))


(def +version+ "2017.03.02")
;;(laces/bootlaces! +version+)

(core/deftask new-repl
  "Start a REPL session for the current project.

  If no bind/host is specified the REPL server will listen on 127.0.0.1 and
  the client will connect to 127.0.0.1.

  If no port is specified the server will choose a random one and the client
  will read the .nrepl-port file and use that.

  The *default-middleware* and *default-dependencies* atoms in the boot.repl
  namespace contain vectors of default REPL middleware and REPL dependencies to
  be loaded when starting the server. You may modify these in your build.boot
  file."

  [s server         bool  "Start REPL server only."
   c client         bool  "Start REPL client only."
   C no-color       bool  "Disable colored REPL client output."
   ;; a pretty-print   edn   "Provide a custom value->string function."
   e eval EXPR      edn   "The form the client will evaluate in the boot.user ns."
   b bind ADDR      str   "The address server listens on."
   H host HOST      str   "The host client connects to."
   i init PATH      str   "The file to evaluate in the boot.user ns."
   I skip-init      bool  "Skip default client initialization code."
   p port PORT      int   "The port to listen on and/or connect to."
   P pod NAME       str   "The name of the pod to start nREPL server in (core)."
   n init-ns NS     sym   "The initial REPL namespace."
   m middleware SYM [sym] "The REPL middleware vector."
   x handler SYM    sym   "The REPL handler (overrides middleware options)."]

  (let [cpl-path (.getPath (core/tmp-dir!))
        srv-opts (->> [:bind :port :init-ns :middleware :handler :pod]
                      (select-keys *opts*))
        cli-opts (-> *opts*
                     (select-keys [:host :port :history])
                     (assoc :standalone true
                            :custom-eval eval
                            :custom-init init
                            :color (and @util/*colorize?* (not no-color))
                            :value-to-string "(fn [x] (println 'stuff') (println x))"
                            :print-value "(fn [x] (println 'stuff') (println x))"
                            :skip-default-init skip-init))
        deps     (remove pod/dependency-loaded? @repl/*default-dependencies*)
        repl-svr (delay (apply core/launch-nrepl (mapcat identity srv-opts)))
        repl-cli (delay (pod/with-call-worker (boot.repl-client/client ~cli-opts)))]
    (comp (core/with-pass-thru [fs]
            (when (or server (not client)) @repl-svr))
          (core/with-post-wrap [_]
            (when (or client (not server)) @repl-cli)))))

(task-options!
  ;; new-repl {:pretty-print
  ;;            (fn [x]
  ;;              (cprint x)
  ;;              (flush)
  jar {:manifest {"Manifest-Version" "1.0"
                  "Built-By" (System/getProperty "user.name")
                  "Created-By" (format "Boot %s" boot.core/*boot-version*)
                  "Build-Jdk" (System/getProperty "java.specification.version")}
                  ;; "Main-Class" "immortals.main"}
       :main 'cure.main}
  push {:repo           "deploy"
        :ensure-branch  "master"
        :ensure-clean   true
        :ensure-tag     (last-commit)
        :ensure-version +version+}
  pom  {:project        'immortals/function_model
        :version        +version+
        :description    "A transformer and environment for function composition"
        :url            "https://github.com/phreed/immortals_xform"
        :scm            {:url "https://github.com/phreed/immortals_xform"}
        :license        {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}
        :developers {"Jules White" ""}
                    "Fred Eisele" "phreed@gmail.com"})

(deftask build
  "Build my project and put it in the local repository."
  [] (build-jar))
  
