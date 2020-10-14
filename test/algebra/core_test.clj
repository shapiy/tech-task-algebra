(ns algebra.core-test
  (:require [clojure.test :refer :all]
            [algebra.core :refer :all]))

;; TODO: optimize
;; (optimize '(+ 10 (* x 0))) => 10
;; (optimize '(+ x (- y 0))) => '(+ x y)

;; (->javascript "example" '(+ 1 (* x x))) =>

(deftest evaluate-test
  (testing "Happy cases"
    (is (= 4 (evaluate {} '(* 2 (+ 1 1)))))
    (is (= 100 (evaluate {:x 10} '(* x x))))
    (is (= 20 (evaluate {:x 10 :y 2} '(* y x)))))

  (testing "Invalid operation"
    (is (= {:error "Invalid inputs"} (evaluate {:x 10} '(42 x x)))))

  (testing "Unary operation is not supported"
    (is (= {:error "Invalid inputs"} (evaluate {:x 10} '(* x)))))

  (testing "Undefined arg"
    (is (= {:error "Undefined arg" :arg "x"} (evaluate {} '(* x x))))))

(deftest ->javascript-test
  (testing "Happy cases"
    (is (= "function example(x) { return (1 + (x * x)); }"
           (->javascript "example" '(+ 1 (* x x)))))
    (is (= "function anotherExample(x, y) { return (1 - (x / y)); }"
           (->javascript "anotherExample" '(- 1 (/ x y))))))

  (testing "Invalid operation"
    (is (= {:error "Invalid inputs"} (->javascript "example" '(42 x x)))))

  (testing "Unary operation is not supported"
    (is (= {:error "Invalid inputs"} (->javascript "example" '(* x))))))
