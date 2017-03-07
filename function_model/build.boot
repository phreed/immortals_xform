

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
     [mvxcvi/puget "1.0.1"]])

(require '[adzerk.boot-logservice :as log-service]
         '[clojure.tools.logging  :as log]
         '[puget.printer :refer [cprint]])


(require '[boot.git :refer [last-commit]]
         '[adzerk.bootlaces :refer [bootlaces! build-jar]]
         '[immortals.function-model :as fm]
         '[cure.core :as cure]
         '[cheshire.core :as json])

(alter-var-root #'log/*logger-factory* 
                (constantly (log-service/make-factory log-config)))


(def +version+ "2017.03.02")
;;(laces/bootlaces! +version+)

(task-options!
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
        :developers {"Jules White" ""
                     "Fred Eisele" "phreed@gmail.com"}})

(deftask build
  "Build my project and put it in the local repository."
  [] (build-jar))
  
