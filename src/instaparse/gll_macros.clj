(ns instaparse.gll-macros)


(defonce PRINT false)
(defmacro dprintln [& body]  
  (when PRINT `(println ~@body)))
(defmacro dpprint [& body]  
  (when PRINT `(clojure.pprint/pprint ~@body)))

(defonce PROFILE false)
(defmacro profile [& body]
  (when PROFILE
    `(do ~@body)))

(defonce TRACE false)
(def ^:dynamic *trace* false)
(defmacro log [tramp & body]
  (when TRACE
    `(when (:trace? ~tramp) (println ~@body))))
(defmacro attach-diagnostic-meta [f metadata]
  (if TRACE
    `(with-meta ~f ~metadata)
    f))
(defmacro bind-trace [trace? body]
  `(if TRACE
     (binding [*trace* ~trace?] ~body)
          ~body))
(defmacro trace-or-false []
  (if TRACE '*trace* false))

(defmacro success [tramp node-key result end]
  `(instaparse.gll/push-result ~tramp ~node-key (instaparse.gll/make-success ~result ~end)))
