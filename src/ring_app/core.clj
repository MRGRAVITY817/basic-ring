(ns ring-app.core
  (:require
   [clojure.core :as c]
   [muuntaja.middleware :as muuntaja]
   [reitit.ring :as reitit]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.util.http-response :as response]
   [selmer.parser :as selmer]
   [selmer.filters :as filters]))

;; Use selmer to create HTML template
(selmer/render-file "hello.html" {:name "Hoon" :items (range 10)})
;; We can also designate the dfeault resource path
;; (selmer.parser/set-resource-path! "/var/html/templates/")
(selmer/render
 "<p>Hello {{user.first}} {{user.last}}!</p>"
 {:user {:first "Hoon" :last "Wee"}})

;; We can add filters, which preprocesses 
;; the data before being rendered in HTML.
(filters/add-filter! :empty? empty?)
(selmer/render
 "{% if files|empty? %}no files{% else %}files{% endif %}"
 {:files []})

(filters/add-filter! :foo (fn [x] [:safe (.toUpperCase x)]))
(selmer/render "{{x|foo}}" {:x "<div>I'm safe</div>"})

(defn html-handler [request-map]
  (response/ok
   (str "<html><body>your IP is: "
        (:remote-addr request-map)
        "</body></html>")))

(defn json-handler [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

;; We can create our own middlewares
(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(def routes
  [["/"         html-handler]
   ["/echo/:id" {:get
                 (fn [{{:keys [id]} :path-params}]
                   (response/ok (str "<p>the value is: " id "</p>")))}]
   ["/api"      {:middleware [wrap-formats]}
    ["/multiply" {:post (fn [{{:keys [a b]} :body-params}]
                          (response/ok {:result (* a b)}))}]]])

(def handler
  (reitit/ring-handler
   (reitit/router routes)
   (reitit/routes
    (reitit/create-resource-handler {:path "/"})
    (reitit/create-default-handler
     {:not-found
      (c/constantly (response/not-found "404 - Page not found"))
      :method-not-allowed
      (c/constantly (response/method-not-allowed "405 - Not allowed"))
      :not-acceptable
      (c/constantly (response/not-acceptable "406 - Not acceptable"))}))))

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


