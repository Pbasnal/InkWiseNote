package com.originb.inkwisenote;

import com.originb.inkwisenote.config.AppSecrets;
import com.originb.inkwisenote.io.ocr.AzureOcrResult;
import com.originb.inkwisenote.io.ocr.OcrService;
import com.originb.inkwisenote.io.utils.BytesFileIoUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class OcrServiceUnitTest {
//    @Test
//    public void testConvertHandwritingToText() throws IOException {
//        File directory = new File(System.getProperty("user.dir"));
//        String testImageFilePath = directory + "/src/test/resources/test_image.jpg";
//        InputStream imageStream = new FileInputStream(testImageFilePath);
//
//        String secretsFilePath = directory + "/src/test/resources/app.secrets";
//        AppSecrets appSecrets = BytesFileIoUtils
//                .readDataFromDisk(secretsFilePath, AppSecrets.class)
//                .get();
//
//        OcrService.AnalyzeImageTask task = new OcrService.AnalyzeImageTask(result -> {
//        }, appSecrets);
//
//        AzureOcrResult response = task.runOcr(imageStream);
//        System.out.println("Response: " + response);
//        assertEquals("This is a test\nnote in a\nunit Test !", response.readResult.content);
//        assertNotNull("Result should not be null", response);
//
//    }
}