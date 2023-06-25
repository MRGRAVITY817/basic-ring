(ns ring-app.core
  (:require [ring.adapter.jetty :as jetty]
            ;; We can bring pre-built middlewares
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.http-response :as response]))

(defn handler [request-map]
  (response/ok (str "<html><body>your IP is: "
                    (:remote-addr request-map)
                    "</body></html>")))

(response/continue) ;; 100
(response/ok)       ;; 200
(response/found "/messages") ;; 302
(response/internal-server-error "failed to complete request") ;; 500

;; We can create our own middlewares
(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn -main []
  (jetty/run-jetty
    ;; To properly use `wrap-reload`, we should read/update
    ;; current handler, which is stored in `var` object.
    ;; We can use
    ;;   (-> handler var wrap-nocache ...)
    ;; But using #'handler is a better way for readability.
   (-> #'handler
       wrap-nocache
       wrap-reload)
   {:port 3333
    :join? false}))


