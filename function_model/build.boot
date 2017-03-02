(set-env!
 :resource-paths #{"res"}
 :source-paths #{"src/clj"}
 :asset-paths #{"asset"}
 :dependencies
   '[[cheshire "5.7.0"]
     [juleswhite/clj-cure "0.1.0"]
     [adzerk/bootlaces "0.1.13"]])

(require '[boot.git :refer [last-commit]]
         '[adzerk.bootlaces :as laces]
         '[immortals.function-model :as fm]
         '[cure.core :as cure]
         '[cheshire.core :as json])

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
        :developers {"Jules White" ""}
                    "Fred Eisele" "phreed@gmail.com"})

(deftask build
  "Build my project and put it in the local repository."
  [] (laces/build-jar))
  
