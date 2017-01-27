(ns clj-img-resize.core-test
  (:require [clojure.test :refer :all]
            [clj-img-resize.core :refer :all]
            [clojure.java.io :as io])
  (:import java.awt.Image
           java.awt.image.BufferedImage
           java.io.ByteArrayOutputStream
           javax.imageio.ImageIO))

(deftest scale-image-test
  (let [[max-height max-width] [100 100]
        test-img (io/file "test/FoodBomb夜食テロ.JPG")
        img-w50-by-h40 (scale-image-to-exact-dimension test-img 50 40 "gif")
        img-w120-by-h130 (scale-image-to-exact-dimension test-img 120 130 "gif")
        img-w120-by-h50 (scale-image-to-exact-dimension test-img 120 50 "gif")
        img-w80-by-h150 (scale-image-to-exact-dimension test-img 80 150 "gif")
        collection [img-w50-by-h40 img-w120-by-h130 img-w120-by-h50 img-w80-by-h150]
        scaled-image-sizes  (->> collection
                                 (map #(-> (scale-image-to-dimension-limit % max-width max-height "gif")
                                           ImageIO/read
                                           get-image-dimentions)))]
    (is (= scaled-image-sizes
           '({:width 50, :height 40} {:width 92, :height 100}
             {:width 100, :height 41} {:width 53, :height 100})))))
