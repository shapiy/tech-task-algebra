(ns algebra.core
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [medley.core :refer :all]
            [schema.core :as s])
  (:import (clojure.lang ExceptionInfo)))

(declare Equation)

(s/defschema Variables
  {s/Keyword s/Num})

(def Operation (s/enum '+ '- '* '/))

(def ArgumentOrOperation
  (s/conditional number? s/Num
                 symbol? s/Symbol
                 list? (s/recursive #'Equation)))

(def Equation
  [(s/one Operation "op")
   (s/one ArgumentOrOperation "arg1")
   (s/one ArgumentOrOperation "arg2")])

;;;

(defn variable? [x]
  (and (symbol? x)
       (re-matches #"[a-z]" (name x))))

(defn- resolve-args [equation m]
  (walk/prewalk
    (fn [x]
      (if (variable? x)
        (get m (-> x name keyword) x)
        x))
    equation))

(defn- evaluate* [[op x y]]
  (try
    (let [f (fn [arg]
              (cond
                (number? arg) arg
                (symbol? arg) (throw (ex-info "Undefined arg" {:arg (name arg)}))
                :else (evaluate* arg)))]
      ;; Resolve function from clojure.core. Can also use Java math here.
      ;; This should work with any function from the core library, but is
      ;; currently limited by the schema.
      (apply (resolve op) [(f x) (f y)]))
    (catch ExceptionInfo e
      (merge {:error (.getMessage e)}
             (ex-data e)))))

(defn evaluate
  "Evaluate algebraic expression.

  ```
  (evaluate {:x 10} '(* x x))
  => 100
  ```"
  [variables equation]
  (let [variables-errors (s/check Variables variables)
        equation-errors (s/check Equation equation)]
    (if (or variables-errors equation-errors)
      ;; Can be more descriptive, of course.
      {:error "Invalid inputs"}
      (-> equation
          (resolve-args variables)
          evaluate*))))

;;;

(defn- javascript-args
  "Convert expression to a JavaScript function.

  ```
  (->javascript \"example\" '(+ 1 (* x x)))
  => \"function example(x) { return (1 + (x * x)); }\"
  ```
  "
  [form]
  (->> (flatten form)
       (filter variable?)
       (map name)
       dedupe
       sort))

(defn- format-javascript-body [[op x y]]
  (let [f (fn [arg]
            (if (or (variable? arg) (number? arg))
              arg
              (format-javascript-body arg)))]
    (str "(" (f x) " " op " " (f y) ")")))

(defn ->javascript [f-name form]
  (let [form-errors (s/check Equation form)]
    (if form-errors
      {:error "Invalid inputs"}
      (str "function " f-name
           "(" (string/join ", " (javascript-args form)) ")"
           " "
           "{ return " (format-javascript-body form) "; }"))))
