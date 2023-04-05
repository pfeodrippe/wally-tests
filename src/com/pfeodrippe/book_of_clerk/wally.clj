(ns com.pfeodrippe.book-of-clerk.wally
  (:require
   [wally.main :as w]
   [wally.selectors :as ws]
   [garden.selectors :as s]))

(comment

  (w/navigate "https://github.clerk.garden/nextjournal/book-of-clerk")

  (w/click (s/a (s/attr= :href "#applying-viewers")))
  (w/in-viewport? (s/h3 (ws/text "🤹🏻 Applying Viewers")))

  (w/click (s/a (s/attr= :href "#recursion")))
  (w/in-viewport? (s/h4 (ws/text "🐢 Recursion")))

  ()

  ())

;; TODO
;; - [ ] Check what's shown in the screen when clicking on some ToC header
;; - [ ] Make it run in the CI
