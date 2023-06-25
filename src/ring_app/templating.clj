(ns ring-app.templating
  (:require
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

;; Define custom tags 
;; {% image "src" %}
(selmer/add-tag!
 :image
 (fn [args _] (str "<img src=" (first args) "/>")))
(selmer/render "{% image \"http://foo.com/logo.jpg\" %}" {})

;; {% uppercase %}
;;   some texts to be uppercase
;; {% enduppercase %}
(selmer/add-tag!
 :uppercase
 (fn [_ _ block]
   (.toUpperCase (get-in block [:uppercase :content])))
 :enduppercase)
(selmer/render "{% uppercase %}foo {{bar}} baz{% enduppercase %}" {:bar "injected"})
