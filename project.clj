(defproject ksql-poc-sa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :source-path "src/clj"
  :source-paths ["src/clj" "src/java"]
  :java-source-paths ["src/java" "test/java"]
  :test-paths ["test"]
  :resources-path "src/resource"
  :resource-paths ["src/resource"]

  :run-aliases {:deploy kafka.deploy.provision}
  :aliases {"deploy" ["run" "-m" "kafka.deploy.provision"]
            "consumer" ["run" "-m" "consumer"]
            "producer" ["run" "-m" "producer"]}

  :repositories {"sonatype" "https://oss.sonatype.org/content/repositories/releases"
                 "jclouds-snapshot" "https://oss.sonatype.org/content/repositories/snapshots"
                 "confluent" "https://packages.confluent.io/maven/"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.4.0"]
                 [org.apache.kafka/kafka-clients "3.0.0"]
                 [com.wjoel/clj-bean "0.2.1"]
                 [ymilky/franzy-json "0.0.1"]
                 [storm "0.9.0.1"]
                 [commons-codec "1.15"]
                 [log4j/log4j "1.2.17"]
                 [org.projectlombok/lombok "1.18.22" :scope "provided"]
                 [clojure.java-time "0.3.2"]
                 [com.fasterxml.jackson.core/jackson-core "2.13.0"]
                 [com.fasterxml.jackson.core/jackson-databind "2.13.0"]
                 [com.fasterxml.jackson.module/jackson-module-parameter-names "2.13.0"]
                 [com.fasterxml.jackson.datatype/jackson-datatype-jsr310 "2.13.0"]
                 [com.fasterxml.jackson.datatype/jackson-datatype-jdk8 "2.13.0"]
                 [io.confluent/kafka-json-serializer "6.2.1"]
                 [criterium "0.4.6"]]

  ;; :aot [clj.beans/InputMessageDTO]
  :classifiers {:javadoc {:source-paths ^:replace [] :aot ^:replace []}
                :sources {:aot ^:replace []}}

  :dev-dependencies [[swank-clojure "1.4.3"] [cider/cider-nrepl "0.9.0"] [refactor-nrepl "1.0.5"]]

;;  :main ^:skip-aot clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[swank-clojure "1.5.0-SNAPSHOT"]]}})
