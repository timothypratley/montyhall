(ns montyhall.ui
  (:require [reagent.core :as r]))

(def doors ["1" "2" "3"])

(defn start [state]
  (assoc state
    :car (str (inc (rand-int (count doors))))
    :choice nil
    :revealed nil
    :action :choosing))

(defonce app-state
  (r/atom (start {})))

(defn start! [ev]
  (swap! app-state start))

(defn choose! [choice]
  (swap! app-state
         (fn [{:keys [car] :as state}]
           (assoc state
             :choice choice
             :revealed (rand-nth (vec (disj (set doors) choice car)))
             :action :revealing))))

(defn swap-or-stay! [next-choice]
  (swap! app-state
         (fn [{:keys [choice car] :as state}]
           (let [won? (= next-choice car)
                 stayed? (= next-choice choice)]
             (-> state
                 (assoc :choice next-choice
                        :action (if won? :won :lost))
                 (update-in [(if won? :wins :losses)
                             (if stayed? :stayed :swapped)]
                            inc))))))

(defn click-door! [ev]
  (let [{:keys [action revealed]} @app-state
        n (-> ev .-currentTarget .-value)]
    (case action
      :choosing (choose! n)
      :revealing (when (not= n revealed)
                   (swap-or-stay! n))
      nil)))

(defn front [label]
  [:span {:style {:position   "absolute"
                  :top        0
                  :left       0
                  :width      "100%"
                  :height     "100%"
                  :background "brown"}}
   [:h1 {:style {:font-size 32}} label]])

(defn back [label background]
  [:span {:style {:position                    "absolute"
                  :transform                   "translateZ(-10px)"
                  :top                         0
                  :left                        0
                  :backface-visibility         "hidden"
                  :-webkit-backface-visibility "hidden"
                  :width                       "100%"
                  :height                      "100%"
                  :background                  background}}
   [:h1 {:style {:font-size 32}} label]])

(defn door [n]
  (let [{:keys [choice revealed action car]} @app-state
        selected? (= n choice)
        car? (= n car)
        revealed? (= n revealed)
        done? (not (contains? #{:choosing :revealing} action))
        front-label (if (and revealed (not revealed?))
                      (if selected? "Stay" "Swap")
                      n)
        back-label (when choice
                     (if car?
                       "\uD83D\uDE97"
                       "\uD83D\uDC10"))
        back-color (if selected?
                     (if done? "lightblue" "brown")
                     "lightgrey")]
    [:div {:style {:perspective 1000
                   :display     "inline-block"
                   :width       100
                   :height      200}}
     [back back-label back-color]
     [:button {:value   n
               :onClick click-door!
               :style   {:position        "relative"
                         :transition      "transform 0.8s"
                         :transform-style "preserve-3d"
                         :transform       (when (or revealed? done?)
                                            "translate(-50%) rotateY(-90deg) translate(50%)")
                         :width           "100%"
                         :height          "100%"}}
      [front front-label]]]))

(defn dec2 [x]
  (.toFixed x 2))

(defn stats [{{stay-win :stayed
               swap-win :swapped}  :wins
              {stay-lose :stayed
               swap-lose :swapped} :losses}]
  (let [stay-rate (when stay-win (dec2 (/ stay-win (+ stay-win stay-lose))))
        swap-rate (when swap-win (dec2 (/ swap-win (+ swap-win swap-lose))))]
    [:table {:border 1
             :style  {:border-collapse "collapse"
                      :margin-left     "auto"
                      :margin-right    "auto"}}
     [:thead
      [:tr
       [:th {:colSpan 3} "Stay"] [:th {:colSpan 3} "Swap"]]
      [:tr
       [:th "Won"] [:th "Lost"] [:th "Rate"]
       [:th "Won"] [:th "Lost"] [:th "Rate"]]]
     [:tbody
      [:tr
       [:td stay-win] [:td stay-lose] [:td stay-rate]
       [:td swap-win] [:td swap-lose] [:td swap-rate]]]]))

(defn game []
  (let [{:keys [action]} @app-state]
    [:div {:style {:text-align "center"}}
     [:h2 (case action
            :choosing "Choose a door:"
            :revealing "Stay or swap?"
            :won "You won!"
            :lost "You lost.")]
     (doall (for [d doors] ^{:key d} [door d]))
     [:br]
     [:br]
     (when (contains? #{:won :lost} action)
       [:div [:button {:style   {:font-size     24
                                 :padding       10
                                 :border-radius 20}
                       :onClick start!}
              "Play again"]])
     [:br]
     [:br]
     [stats @app-state]]))
