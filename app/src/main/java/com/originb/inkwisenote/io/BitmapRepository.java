package com.originb.inkwisenote.io;

import android.graphics.Bitmap;
import com.google.android.gms.common.util.Strings;
import com.originb.inkwisenote.constants.BitmapScale;
import com.originb.inkwisenote.constants.Returns;
import com.originb.inkwisenote.filemanager.BitmapFileManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.*;


// bitmapTimestamp is the id of the note.
// All related files to one note will have the same
// timestamp.
public class BitmapRepository {
    private final File directory;

    // bitmapTimestamp -> bitmap
    private Map<Long, BitmapInfo> thumbnails;
    private Map<Long, BitmapInfo> fullImages;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BitmapInfo {
        private String bitmapPath;
        private Bitmap bitmap;

        public BitmapInfo withBitmap(Bitmap bitmap) {
            return new BitmapInfo(bitmapPath, bitmap);
        }
    }

    // Constructor to set the directory where notes will be saved
    public BitmapRepository(File directory) {
        this.directory = directory;
        // Ensure the directory exists
        if (!directory.exists()) {
            directory.mkdirs();
        }
        thumbnails = new HashMap<>();
        fullImages = new HashMap<>();
    }

    public void loadAllAsThumbnails() {
        File[] bitmapFiles = directory.listFiles((dir, name) -> name.endsWith(".png"));

        if (Objects.isNull(bitmapFiles)) return;

        thumbnails.clear();
        for (int i = 0; i < bitmapFiles.length; i++) {
            String bitmapName = bitmapFiles[i].getName();
            Bitmap bitmap = BitmapFileManager.readBitmapFromFile(bitmapFiles[i].getAbsolutePath(),
                            BitmapScale.THUMBNAIL.getResult())
                    .orElse(null);

            if (Objects.isNull(bitmap)) continue;
            String bitmapNameWithoutExtension = bitmapName.replace(".png", "");
            Long bitmapId = parseBitmapIdFromFileName(bitmapNameWithoutExtension);
            if (Objects.isNull(bitmapId)) continue;

            thumbnails.put(bitmapId, new BitmapInfo(bitmapFiles[i].getPath(), bitmap));
        }
    }

    public void deleteBitmap(Long bitmapId) {
        if (Objects.isNull(bitmapId)) {
            return;
        }

        if (thumbnails.containsKey(bitmapId)) {
            BitmapInfo bitmapInfo = thumbnails.get(bitmapId);
            BitmapFileManager.deleteBitmap(bitmapInfo.getBitmapPath());
            thumbnails.remove(bitmapId);
        }
        fullImages.remove(bitmapId);

    }

    public Optional<Bitmap> getThumbnail(Long bitmapId) {
        if (Objects.isNull(bitmapId)) {
            return Optional.empty();
        }

        if (thumbnails.containsKey(bitmapId)) {
            BitmapInfo bitmapInfo = thumbnails.get(bitmapId);
            return BitmapFileManager.readBitmapFromFile(bitmapInfo.getBitmapPath(),
                    BitmapScale.THUMBNAIL.getResult());
        }

        return Optional.empty();
    }

    public Optional<Bitmap> getFullBitmap(Long bitmapId) {
        if (Objects.isNull(bitmapId)) {
            return Optional.empty();
        }

        if (fullImages.containsKey(bitmapId)) {
            return Optional.ofNullable(fullImages.get(bitmapId))
                    .map(b -> b.bitmap);
        } else if (thumbnails.containsKey(bitmapId)) {
            BitmapInfo bitmapInfo = thumbnails.get(bitmapId);
            BitmapFileManager.readBitmapFromFile(bitmapInfo.getBitmapPath(),
                            BitmapScale.FULL_SIZE.getResult())
                    .ifPresent(bitmap -> fullImages.put(bitmapId, bitmapInfo.withBitmap(bitmap)));

            return Optional.ofNullable(fullImages.get(bitmapId))
                    .map(b -> b.bitmap);

        }

        return Optional.empty();
    }

    public Returns updateBitmap(Long bitmapId, Bitmap bitmap) {
        if (Objects.isNull(bitmapId)) {
            return Returns.INVALID_ARGUMENTS;
        }

        if (!thumbnails.containsKey(bitmapId)) {
            return Returns.BITMAP_DOESNT_EXISTS;
        }

        BitmapInfo bitmapInfo = thumbnails.get(bitmapId);
        BitmapInfo updateBitmap = bitmapInfo.withBitmap(bitmap);
        thumbnails.put(bitmapId, updateBitmap);
        fullImages.put(bitmapId, updateBitmap);
        BitmapFileManager.writeDataToDisk(bitmapInfo.bitmapPath, bitmap);
        return Returns.SUCCESS;

    }

    public Returns saveBitmap(Long bitmapId, String path, String filename, Bitmap bitmap) {
        if (Objects.isNull(bitmapId)
                || Strings.isEmptyOrWhitespace(path)
                || Objects.isNull(bitmap)) {
            return Returns.INVALID_ARGUMENTS;
        }

        if (thumbnails.containsKey(bitmapId)) {
            return Returns.BITMAP_ALREADY_EXISTS;
        }

        path = path + "/" + filename + ".png";
        BitmapFileManager.writeDataToDisk(path, bitmap);
        BitmapInfo bitmapInfo = new BitmapInfo(path, bitmap);
        thumbnails.put(bitmapId, bitmapInfo);
        fullImages.put(bitmapId, bitmapInfo);
        return Returns.SUCCESS;
    }

    private static Long parseBitmapIdFromFileName(String noteNameWithoutExtension) {
        if (noteNameWithoutExtension.contains("-")) {
            return Long.parseLong(noteNameWithoutExtension.split("-")[1]);
        } else if (noteNameWithoutExtension.contains("_")) {
            return Long.parseLong(noteNameWithoutExtension.split("_")[1]);
        }
        return null;
    }
}