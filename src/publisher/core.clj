(ns publisher.core
  (:require [nostr.edufeed :as edufeed]
            [nostr.event-composer :as event-composer]
            [clojure.java.io :as io]
            [hato.websocket :as ws]
            [cheshire.core :as json]))

(defn create-websocket [relay]
  ;; Create the WebSocket connection and return it
  (let [ws @(ws/websocket relay
                          {:on-message (fn [ws msg last?]
                                         (when last?
                                           (println msg)))
                           :on-close   (fn [ws status reason]
                                         (println "WebSocket closed with reason:" reason))})]
    ws))

(defn send-to-relay [ws raw-event]
  (let [signed-event (event-composer/body->event raw-event "9109ffc9fcddb1086e153ed23ff91c4f69f726178bf959f09b1f29c7569e24a1")
        _ (println (json/generate-string signed-event))]
    (ws/send! ws (json/generate-string signed-event))))

(defn save-to-jsonl [data file-path]
  (with-open [writer (io/writer file-path :append true)]
    (.write writer (str (json/generate-string data) "\n"))))

(defn process-json-line [raw-event]
  (save-to-jsonl raw-event "events.jsonl"))

(defn process-json-lines-file [file-path relay]
  (let [ws (create-websocket relay)]
    (with-open [reader (io/reader file-path)]
      (doseq [line (line-seq reader)]
        (let [json-data (json/parse-string line true)
              raw-event (edufeed/transform-amb-to-30142-event (:_source json-data))]
          (process-json-line raw-event)
          (send-to-relay ws raw-event))))))

(defn run [{:keys [path relay]}]
  (println "Args:" path relay)
  (process-json-lines-file path relay ))
