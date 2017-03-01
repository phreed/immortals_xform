(set-env!
 :resource-paths #{"res"}
 :source-paths #{"src/clj"}
 :dependencies
   '[[cheshire "5.7.0"]])

(task-options!
  pom {:project 'immortals
       :version "0.1.0"}
  jar {:manifest {"Foo" "bar"}})

(deftask build
  "Build my project and put it in the local repository."
  []
  (comp (pom) (jar) (install)))
  
