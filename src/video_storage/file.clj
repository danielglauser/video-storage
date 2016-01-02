(ns video-storage.file)

(defn extension [file-or-filename]
  (last
   (clojure.string/split (filename file-or-filename) #"\.")))

(defn filename
  "If passed an instance of java.io.File returns the filename,
  otherwise returns what it was passed."
  [file-or-filename]
  (if (instance? java.io.File file-or-filename)
    (.getName file-or-filename)
    file-or-filename))
