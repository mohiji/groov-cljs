(defproject eris "0.0.0-SNAPSHOT"
  :description "Rendering groov-like pages with ClojureScript and React"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [org.omcljs/om "0.9.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.207"]
                 [cljs-ajax "0.3.13"]
                 [figwheel "0.3.7"]]

  :jvm-opts ["-Xmx1G"]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.7"]]

  :clean-targets ^{:protect false} ["target"
                                    "resources/public/js/app.js"
                                    "resources/public/js/app.js.map"
                                    "resources/public/js/app-dev.js"
                                    "resources/public/js/dev"
                                    "resources/public/js/release"]
  :figwheel {:http-server-root "public"
             :port 3449
             :css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "dev"]
                        :compiler {:output-to "resources/public/js/app-dev.js"
                                   :output-dir "resources/public/js/dev/out"
                                   :optimizations :none
                                   :asset-path "js/dev/out"
                                   :cache-analysis true
                                   :source-map true
                                   :source-map-timestamp true}}
                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/release/out"
                                   :source-map "resources/public/js/app.js.map"
                                   :main "groov.core"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
