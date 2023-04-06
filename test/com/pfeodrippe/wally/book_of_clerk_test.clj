(ns com.pfeodrippe.wally.book-of-clerk-test
  (:require
   [clojure.test :refer [deftest testing is use-fixtures]]
   [garden.selectors :as s]
   [wally.main :as w]
   [wally.selectors :as ws]))

(defn headless-mode
  [f]
  (w/with-page (w/make-page {:headless true})
    (f)))

(use-fixtures :once headless-mode)

(deftest book-of-clerk

  ;; Navigate to a page.
  (w/navigate "https://github.clerk.garden/nextjournal/book-of-clerk")

  ;; Click on a element.
  ;; We use garden selectors to do queries. In this case, we are querying (for clicking on it)
  ;; for the element that is an `a` and has a `href` pointing to the
  ;; `#applying-viewers` ID,  this element is in the ToC of the book of clerk (in the
  ;; sidebar).
  ;; In raw form, it's equivalent to `(s/selector "a[href=\"#applying-viewers\"]")`
  ;; (you can inspect the form built using the REPL!).
  ;; Check https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Selectors and
  ;; https://cljdoc.org/d/garden/garden/1.3.10/doc/readme.
  (w/click (s/a (s/attr= :href "#applying-viewers")))

  ;; After clicking above, we want to make sure that the right content is being
  ;; displayed in the browser viewport (which it's what the user sees while
  ;; scrolling through the page).
  (is (w/in-viewport? (s/h3 (ws/text "🤹🏻 Applying Viewers"))))

  (w/click (s/a (s/attr= :href "#recursion")))
  (is (w/in-viewport? (s/h4 (ws/text "🐢 Recursion"))))

  ;; This should fail now as the page scrolled until the Recursion section.
  ;; (w/in-viewport? (s/h3 (ws/text "🤹🏻 Applying Viewers")))
  )

;; TODO
;; - [x] Check what's shown in the screen when clicking on some ToC header
;; - [ ] Make it run in the CI
