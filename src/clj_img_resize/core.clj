(ns clj-img-resize.core
  (:require [clojure.java.io :as io])
  (:import java.awt.Image
           java.awt.image.BufferedImage
           java.io.ByteArrayOutputStream
           javax.imageio.ImageIO
           sun.awt.image.ToolkitImage))

(defn get-image-dimentions
  "This is public because it is useful for testing and debugging"
  [^BufferedImage buffered-image]
  {:width (.getWidth buffered-image)
   :height (.getHeight buffered-image)})

(defn- compare-image-dimensions-as-ratios [{:keys [width height max-width max-height]}]
  {:width-ratio (/ width max-width)
   :height-ratio (/ height max-height)})

(defn- select-how-to-scale-from-ratio
  "A higher order fn that returns a flavor of imageio.getscaledinstance method that
   ensures the image is within the specified limit and that retains the width-height ratio"
  [{:keys [width height max-width max-height] :as args}]
  (let [{:keys [width-ratio height-ratio]} (compare-image-dimensions-as-ratios args)
        template-fn (fn [^BufferedImage buffered-image w h]
                      (.getScaledInstance buffered-image w h (Image/SCALE_FAST)))]
    (cond
      ;; Both width and heights are within the limit
      (and (<= width-ratio 1) (<= height-ratio 1))
      (fn [buffered-image _ _]
        (template-fn buffered-image width height))

      ;; Both width and height violate the limit
      (and (> width-ratio 1) (> height-ratio 1))
      (if (> width-ratio height-ratio)
        (fn [buffered-image w _]
          (template-fn buffered-image max-width -1))
        (fn [buffered-image _ h]
          (template-fn buffered-image -1 max-height)))

      ;; Width violates the limit, but height is within the limit
      (> width-ratio 1) (fn [buffered-image w _]
                          (template-fn buffered-image w -1))

      ;; Height violates the limit, but the width is within the limit
      (> height-ratio 1) (fn [buffered-image _ h]
                           (template-fn buffered-image -1 h))

      ;; Something is not right
      :default (throw (ex-info "not sure what to do...")))))

(defn scale-image-to-exact-dimension
  "Returns a BufferedInputstream instance that represents the original image with the specified width and heights
   The w and h are specified in pixels.
   file-type is a string of of the desired output file type such as \"jpeg\" \"png\" or \"
   Use this if you know the exact end dimension of the end result you desire"
  [input-stream-or-file w h ^String file-type]
  (with-open [out-stream (ByteArrayOutputStream.)]
    (let [^BufferedImage image (ImageIO/read input-stream-or-file)
          ^ToolkitImage scaled-instance (.getScaledInstance image w h (Image/SCALE_FAST))
          ^BufferedImage buffer (BufferedImage. (.getWidth scaled-instance)
                                                (.getHeight scaled-instance)
                                                BufferedImage/TYPE_INT_RGB)]
      (-> (.createGraphics buffer)
          (.drawImage scaled-instance 0 0 nil))
      (ImageIO/write buffer file-type out-stream)
      (io/input-stream (.toByteArray out-stream)))))

(defn scale-image-to-dimension-limit
  "Returns a BufferedInputstream representing a thumbnail.
   Resulting image will be scaled such that:
   1. width/height ratio is retained
   2. Both the image width and image height are less than or equal to the input width and height.

   The input stream or file must be a valid image.
   The units of dimensions are in pixels

   Use this if you want an image that is within width and height,
   while retaining its proportion."
  [ input-stream-or-file width height ^String file-type]
  (with-open [out-stream (ByteArrayOutputStream.)]
    (let [image (ImageIO/read input-stream-or-file)
          scaling-fn (-> image
                         get-image-dimentions
                         (assoc :max-height height :max-width width)
                         select-how-to-scale-from-ratio)
          ^ToolkitImage scaled-instance (scaling-fn image width height)
          buffer (BufferedImage. (.getWidth scaled-instance)
                                 (.getHeight scaled-instance)
                                 BufferedImage/TYPE_INT_RGB)]
      (-> (.createGraphics buffer)
          (.drawImage scaled-instance 0 0 nil))
      (ImageIO/write buffer file-type out-stream)
      (io/input-stream (.toByteArray out-stream)))))
