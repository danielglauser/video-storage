(ns video-storage.metadata
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [pantomime.extract :as extract]
            [clj-time.core :as tm]
            [clj-time.format :as frmt]
            [video-storage.file :refer [extension filename]]))

(def metadata-filename "meta.edn")

(defn sort-by-duration
  "Returns a vector of metadata sorted by :duration."
  [metadata]
  (sort-by :duration metadata))

(defn sort-by-time
  "Returns a vector of metadata sorted by :datetime."
  [metadata]
  (sort (fn [left right]
          (let [d1 (frmt/parse (:datetime left))
                d2 (frmt/parse (:datetime right))]
            (tm/before? d1 d2)))
        metadata))

(defn save-metadata [path metadata]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir))
    (spit (str path (java.io.File/separator) metadata-filename)
          (pr-str metadata))))

(defn read-metadata [path]
  (let [dir (io/file path)]
    (if (not (.exists dir))
      (throw (Exception. (str "Path " path " doesn't exist.")))
      (-> (str path (java.io.File/separator) metadata-filename)
          slurp
          edn/read-string))))

(defn extract-metadata* [tags file]
  {:pre [(vector? tags)
         (instance? java.io.File file)]}
  (let [ext (extension file)]
    (when (= "mp4" (clojure.string/lower-case ext))
      (let [extracted-meta (extract/parse file)]
        {:name (filename file)
         :type :video
         :duration (first (:xmpdm:duration extracted-meta))
         :datetime (first (:date extracted-meta))
         :tags tags}))))

(defn extract-metadata
  "Given a directory containing mp4 files this fn will extract
  metadata from the files. This fn requires that at least one tag is
  specified so we always know what room a video file is from.
  Returns a seq of the metadata."
  [tags path]
  {:pre [(vector? tags)]}
  (when (= 0 (count tags))
    (throw (Exception. "Please pass at least one tag to extract-metadata.")))
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (throw (Exception. (str "Directory " path " does not exist."))))
    (let [files (file-seq dir)]
      (->> (map (partial extract-metadata* tags) files)
           (remove nil?)))))
