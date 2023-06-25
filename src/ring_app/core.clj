(ns ring-app.core
  (:require
   [muuntaja.middleware :as muuntaja]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.util.http-response :as response]))

(defn html-handler [request-map]
  (response/ok
   (str "<html><body>your IP is: "
        (:remote-addr request-map)
        "</body></html>")))

(defn json-handler [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

(def handler json-handler)

;; We can create our own middlewares
(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(defn -main []
  (jetty/run-jetty
    ;; To properly use `wrap-reload`, we should read/update
    ;; current handler, which is stored in `var` object.
    ;; We can use
    ;;   (-> handler var wrap-nocache ...)
    ;; But using #'handler is a better way for readability.
   (-> #'handler
       wrap-nocache
       wrap-formats
       wrap-reload)
   {:port 3333
    :join? false}))


