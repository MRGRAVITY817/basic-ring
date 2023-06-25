(ns ring-app.core
  (:require [ring.adapter.jetty :as jetty]
            ;; We can bring pre-built middlewares
            [ring.middleware.reload :refer [wrap-reload]]))

(defn handler [request-map]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "<html><body>Wow, your IP is: "
              (:remote-addr request-map)
              "</body></html>")})

;; We can create our own middlewares
(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn -main []
  (jetty/run-jetty
   (-> #'handler
       wrap-nocache
       wrap-reload)
   {:port 3333
    :join? false}))


