(ns chatter.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [hiccup.page :as page]
            [hiccup.form :as form]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :as jetty]
            [ring.util.anti-forgery :as anti-forgery]
            [environ.core :refer [env]]))

(def chat-messages
  (atom '()))

(defn init []
  (println "chatter is starting"))

(defn destroy []
  (println "chatter is shutting down"))

(defn generate-message-view
  "This generates the HTML for displaying messages"
  [messages]
  (page/html5
   [:head
    [:title "chatter"]
    (page/include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css")
    (page/include-js  "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js")
    (page/include-css "/chatter.css")]
   [:body [:div.container.bg-success
    [:h1.text-primary "Lets Chat"]
    [:p
     (form/form-to
      [:post "/"]
      [:div.form-group
       [:span.text-danger [:strong "Name: "]]
       (form/text-field {:class "form-control"} "name")]
      [:div.form-group
       [:span.text-primary [:strong "Favorite Color: "]]
       (form/text-field {:class "form-control"} "color")]
      [:div.form-group
       [:span.text-danger [:strong "Favorite Animal: "]]
       (form/text-field {:class "form-control"} "animal")]
      [:div.form-group
       [:span.text-primary [:strong "Message: "]]
       (form/text-field {:class "form-control"} "msg")]
      (form/submit-button
       {:class "btn btn-info btn-lg"} "Submit"))]
    [:p
     [:table#messages.table.table-bordered.table-striped.
      (map (fn [m] [:tr {:style (str "color:"(:color m))} [:td (:name m)] [:td (:color m)] [:td (:animal m)] [:td (:message m)]]) messages)]]]]))

(defn update-messages!
  "This will update a message list atom"
  [messages name color animal message]
  (swap! messages conj  {:name name :color color :animal animal :message message}))

(defroutes app-routes
  (GET "/" [] (generate-message-view @chat-messages))
  (POST "/" {params :params}
        (let [name-param (get params "name")
              color-param (get params "color")
              animal-param (get params "animal")
              msg-param (get params "msg")
              new-messages (update-messages! chat-messages name-param color-param animal-param msg-param)]
        (generate-message-view new-messages)
          ))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (wrap-params app-routes))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty #'app {:port port :join? false})))
