(ns producer
  (:require
   [clojure.java.io :as jio]
   [java-time :refer [instant]]
   [clj-time.core :as t])
  (:import
   (java.util Properties)
   (org.joda.time DateTime Instant)
   (clj.beans InputMessageDTO InputMessageDTO$ChargeType)
   (org.apache.kafka.clients.admin AdminClient NewTopic)
   (org.apache.kafka.clients.producer Callback KafkaProducer ProducerRecord)
   (org.apache.kafka.common.errors TopicExistsException)))

(def products [{:id 0 :amount 5 :unit "kg"}
               {:id 1 :amount 500 :unit "ml"}
               {:id 2 :amount 1 :unit "unit"}
               {:id 3 :amount 2 :unit "unit"}
               {:id 4 :amount 5 :unit "kg"}
               {:id 5 :amount 37 :unit "kg"}
               {:id 6 :amount 42 :unit "kg"}
               {:id 7 :amount 12 :unit "ml"}
               {:id 8 :amount 5 :unit "kg"}
               {:id 9 :amount 5 :unit "km"}
               {:id 10 :amount 5 :unit "m"}
               {:id 11 :amount 5 :unit "kg"}
               {:id 12 :amount 5 :unit "kg"}
               {:id 13 :amount 5 :unit "kg"}
               {:id 14 :amount 5 :unit "kg"}])

(def customers [{:id "c-0"}
                {:id "c-1"}
                {:id "c-2"}
                {:id "c-3"}
                {:id "c-4"}
                {:id "c-5"}])

(def usages [InputMessageDTO$ChargeType/USAGE InputMessageDTO$ChargeType/OTF])

(defn- build-properties [config-fname]
  (with-open [config (jio/reader config-fname)]
    (doto (Properties.)
      (.load config))))

(defn- create-topic! [topic partitions replication cloud-config]
  (let [ac (AdminClient/create cloud-config)]
    (try
      (.createTopics ac [(NewTopic. ^String topic  (int partitions) (short replication))])
      ;; Ignore TopicExistsException, which would get thrown if the topic was previously created
      (catch TopicExistsException e nil
             (.printStackTrace e))
      (finally
        (.close ac)))))

(defn producer! [config-fname topic]
  (let [props          (build-properties config-fname)
        print-ex       (comp println (partial str "Failed to deliver message: "))
        print-metadata #(printf "Produced record to topic %s partition [%d] @ offest %d\n"
                                (.topic %)
                                (.partition %)
                                (.offset %))
        create-msg #(let [startDate (DateTime. (.toEpochMilli (instant)))
                          product (rand-nth products)
                          bn (InputMessageDTO. (str "id-" %)
                                               (instant (.getMillis (t/plus startDate (t/days (/ % 32)))))
                                               (get (rand-nth customers) :id)
                                               (str (get product :id))
                                               (rand-nth usages)
                                               (int (get product :amount))
                                               (get product :unit))

                          pr (ProducerRecord. topic (.getId bn) bn)]
                      pr)]

    (with-open [producer (KafkaProducer. props)]
      (create-topic! topic 1 3 props)
      (let [;; Using callbacks to handle the result of a send:
            callback (reify Callback
                       (onCompletion [this metadata exception]
                         (if exception
                           (print-ex exception)
                           (print-metadata metadata))))]
        (doseq [i (range 1024 2048)]
          (.send producer (create-msg i) callback)
          ;; flushing once per 25 records collected
          (if (identical? (mod i 25) 0) (doall (.flush producer) (println "Flushing")) (println "Collecting 25 entries")))
        ;; Or we could wait for the returned futures to resolve, like this:
        (let [futures (doall (map #(.send producer (create-msg %)) (range 5 10)))]
          (.flush producer)
          (while (not-every? future-done? futures)
            (Thread/sleep 50))
          (doseq [fut futures]
            (try
              (let [metadata (deref fut)]
                (print-metadata metadata))
              (catch Exception e
                (print-ex e))))))
      (printf "10 messages were produced to topic %s!\n" topic))))

(defn -main [& args]
  (apply producer! args))


