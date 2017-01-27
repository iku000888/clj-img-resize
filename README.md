# clj-img-resize

A small library to resize dimentions of images.

[![Clojars Project](https://img.shields.io/clojars/v/clj-img-resize.svg)](https://clojars.org/clj-img-resize)

## Usage

There are two functions that can be used in the following fashion:

```clojure
(ns foo.bar
  (:require [clj-img-resize.core :as i]
            [clojure.java.io :as io]))

(-> (io/file "test/FoodBomb夜食テロ.JPG") ;;Any image
    (i/scale-image-to-exact-dimension 20 20 "jpeg") ;; Width, Height, and output file format such as jpeg/gif/png
    (io/copy (io/file "20by20.jpeg")) ;; Write the resulting stream into a file
    )
```

`scale-image-to-exact-dimension` will force the resulting image to be the specified width and height in pixels, ignoring the original proportion.

`scale-image-to-dimension-limit` will change the dimension of the image so that:

1. The original proportion is retained.
2. Either one of the resulting width or height matches the width or height of the specified limit, based on which of the original length is closer to the limit.

## License

Copyright © 2017 Ikuru K

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
