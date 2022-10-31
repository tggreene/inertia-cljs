(ns tggreene.inertia-cljs
  (:require
   ["@inertiajs/inertia-react" :refer [createInertiaApp useForm usePage]]
   ["react" :as react]
   [applied-science.js-interop :as j]))

(defn ->js-shallow
  [x]
  (cond
    (map? x) (apply js-obj (mapcat (fn [[k v]] [(name k) v]) x))
    (sequential? x) (apply array (seq x))
    :else x))

(defn set-layout!
  ([page-component layout-component]
   (set-layout! page-component layout-component nil))
  ([^js page-component layout-component layout-props]
   (set! (.-layout page-component)
         (fn [page]
           (react/createElement
            layout-component
            (->js-shallow layout-props)
            page)))))

(defn simple-resolve-fn
  "Simple resolve implementation that takes a page-fn and returns a resolve-fn
   optionally takes a "
  [{:keys [page-fn layout-component layout-props]}]
  (fn resolve-fn
    [name]
    (if-let [^js page-component (page-fn name)]
      (do
        (when layout-component
          (set-layout! page-component layout-component layout-props))
        page-component)
      (js/console.error (str "No page called " name " exists")))))

(defn simple-setup-fn
  [{:keys [react-root-renderer root-component root-props]}]
  (j/fn setup-fn
    [^:js {:keys [App el props]}]
    (react-root-renderer {:inertia-props {:App App
                                          :el el
                                          :props props}
                          :root-component root-component
                          :root-props root-props})))

(defn create-inertia-app
  "Thin wrapper around createInertiaApp"
  [{:keys [resolve-fn
           title-fn
           setup-fn]}]
  (createInertiaApp
   #js {:resolve resolve-fn
        :title title-fn
        :setup setup-fn}))

(defn simple-inertia-app
  "Simple wrapper around createInertiaApp with sensible defaults only page-fn
  and react-root-renderer are required."
  [{:keys [page-fn
           title-fn
           layout-component
           layout-props
           root-component
           root-props
           react-root-renderer]}]
  (createInertiaApp
   #js {:resolve (simple-resolve-fn {:page-fn page-fn
                                     :layout-component layout-component
                                     :layout-props layout-props})
        :title title-fn
        :setup (simple-setup-fn {:react-root-renderer react-root-renderer
                                 :root-component root-component
                                 :root-props root-props})}))

(defn use-form
  [initialData]
  (let [{:keys [data setData post processing errors]}
        (j/lookup (useForm (clj->js initialData)))]
    {:data (js->clj data :keywordize-keys true)
     :setData #(setData (name %1) %2)
     :post post
     :processing processing
     :errors (js->clj errors :keywordize-keys true)}))

(defn use-page
  []
  (let [page (usePage)]
    (js->clj page :keywordize-keys true)))
