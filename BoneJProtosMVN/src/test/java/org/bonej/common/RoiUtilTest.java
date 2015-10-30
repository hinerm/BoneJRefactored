package org.bonej.common;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the RoiUtil class
 * @author <a href="mailto:rdomander@rvc.ac.uk">Richard Domander</a>
 */
public class RoiUtilTest {
    RoiManager mockRoiManager = mock(RoiManager.class);

    @Before
    public void setUp()
    {
        mockRoiManager.reset();
    }

    @Test
    public void testGetSliceRoi() throws Exception {
        final int BAD_SLICE_NUMBER = 0;
        final int NO_ROI_SLICE_NO = 2;
        final int SINGLE_ROI_SLICE_NO = 3;
        final int MULTI_ROI_SLICE_NO = 4;

        // RoiManager.getSliceNumber tries to parse the number of the slice from the label of the Roi it's given.
        // It doesn't - for example - check the slice attribute of the given Roi...
        final String singleRoiLabel = "000" + SINGLE_ROI_SLICE_NO + "-0000-0001";
        final String multiRoi1Label = "000" + MULTI_ROI_SLICE_NO + "-0000-0001";
        final String multiRoi2Label = "000" + MULTI_ROI_SLICE_NO + "-0000-0002";
        final String noSliceLabel = "NO_SLICE";

        Roi singleRoi = new Roi(10, 10, 10, 10);
        singleRoi.setName(singleRoiLabel);

        Roi multiRoi1 = new Roi(10, 10, 10, 10);
        multiRoi1.setName(multiRoi1Label);

        Roi multiRoi2 = new Roi(30, 30, 10, 10);
        multiRoi2.setName(multiRoi2Label);

        Roi noSliceRoi = new Roi(50, 50, 10, 10);
        noSliceRoi.setName(noSliceLabel);

        Roi rois[] = {singleRoi, multiRoi1, multiRoi2, noSliceRoi};

        when(mockRoiManager.getSliceNumber(anyString())).thenCallRealMethod();
        when(mockRoiManager.getRoisAsArray()).thenReturn(rois);

        // Bad slice number
        ArrayList<Roi> resultRois = RoiUtil.getSliceRoi(mockRoiManager, BAD_SLICE_NUMBER);

        assertEquals("Bad slice number should return no ROIs", 0, resultRois.size());

        // Slice with no (associated) Rois
        resultRois = RoiUtil.getSliceRoi(mockRoiManager, NO_ROI_SLICE_NO);
        assertEquals("Wrong number of ROIs returned", 1, resultRois.size());
        assertEquals("Wrong ROI returned", noSliceLabel, resultRois.get(0).getName());

        // Slice with one Roi
        resultRois = RoiUtil.getSliceRoi(mockRoiManager, SINGLE_ROI_SLICE_NO);

        assertEquals("Wrong number of ROIs returned", 2, resultRois.size());
        assertEquals("Wrong ROI returned, or ROIs in wrong order", singleRoiLabel, resultRois.get(0).getName());
        assertEquals("Wrong ROI returned, or ROIs in wrong order", noSliceLabel, resultRois.get(1).getName());

        // Slice with multiple Rois
        resultRois = RoiUtil.getSliceRoi(mockRoiManager, MULTI_ROI_SLICE_NO);

        assertEquals("Wrong number of ROIs returned", 3, resultRois.size());
        assertEquals("Wrong ROI returned, or ROIs in wrong order", multiRoi1Label, resultRois.get(0).getName());
        assertEquals("Wrong ROI returned, or ROIs in wrong order", multiRoi2Label, resultRois.get(1).getName());
        assertEquals("Wrong ROI returned, or ROIs in wrong order", noSliceLabel, resultRois.get(2).getName());
    }

    @Test
    public void testGetLimits() throws Exception
    {
        final int NUM_LIMITS = 6;
        final int MIN_Z_INDEX = 4;
        final int MAX_Z_INDEX = 5;

        final int ROI1_X = 10;
        final int ROI1_Y = 10;
        final int ROI1_WIDTH = 30;
        final int ROI1_HEIGHT = 60;
        final int ROI2_X = 20;
        final int ROI2_Y = 5;
        final int ROI2_WIDTH = 40;
        final int ROI2_HEIGHT = 30;

        final int MIN_X = ROI1_X;
        final int MIN_Y = ROI2_Y;
        final int MAX_X = ROI2_X + ROI2_WIDTH;
        final int MAX_Y = ROI1_Y + ROI1_HEIGHT;
        final int MIN_Z = 2;
        final int MAX_Z = 3;

        final String roi1Label = "000" + MIN_Z + "-0000-0001";
        final String roi2Label = "000" + MAX_Z + "-0000-0001";

        Roi roi1 = new Roi(ROI1_X, ROI1_Y, ROI1_WIDTH, ROI1_HEIGHT);
        roi1.setName(roi1Label);

        Roi roi2 = new Roi(ROI2_X, ROI2_Y, ROI2_WIDTH, ROI2_HEIGHT);
        roi2.setName(roi2Label);

        Roi rois[] = {roi1, roi2};

        when(mockRoiManager.getSliceNumber(anyString())).thenCallRealMethod();
        when(mockRoiManager.getRoisAsArray()).thenReturn(rois);

        // Empty RoiManager
        when(mockRoiManager.getCount()).thenReturn(0);

        int limitsResult[] = RoiUtil.getLimits(mockRoiManager);
        assertEquals(null, limitsResult);

        // All valid ROIs
        when(mockRoiManager.getCount()).thenReturn(rois.length);

        limitsResult = RoiUtil.getLimits(mockRoiManager);
        assertNotEquals(null, limitsResult);
        assertEquals(NUM_LIMITS, limitsResult.length);
        assertEquals(MIN_X, limitsResult[0]);
        assertEquals(MAX_X, limitsResult[1]);
        assertEquals(MIN_Y, limitsResult[2]);
        assertEquals(MAX_Y, limitsResult[3]);
        assertEquals(MIN_Z, limitsResult[MIN_Z_INDEX]);
        assertEquals(MAX_Z, limitsResult[MAX_Z_INDEX]);

        // A ROI without a slice number (z-index)
        Roi badZRoi = new Roi(80, 80, 10, 10);
        //if the label of a roi doesn't follow a certain format, then RoiManager.getSliceNumber returns -1
        badZRoi.setName("BAD_LABEL");
        Roi roisWithBadZ[] = {roi1, roi2, badZRoi};

        when(mockRoiManager.getRoisAsArray()).thenReturn(roisWithBadZ);

        limitsResult = RoiUtil.getLimits(mockRoiManager);
        assertNotEquals(null, limitsResult);
        assertEquals(RoiUtil.DEFAULT_Z_MIN, limitsResult[MIN_Z_INDEX]);
        assertEquals(RoiUtil.DEFAULT_Z_MAX, limitsResult[MAX_Z_INDEX]);
    }

    @Test
    public void testCropStack() throws Exception
    {
        final int ROI_WIDTH = 2;
        final int ROI_HEIGHT = 2;
        final int TEST_COLOR_COUNT = 8;
        final int TEST_COLOR = 0x40;
        final int BACKGROUND_COLOR = 0x00;
        final int BACKGROUND_COLOR_COUNT = 46;

        int limits[] = {2, 8, 2, 5, 1, 3};

        Roi roi1 = new Roi(2, 2, ROI_WIDTH, ROI_HEIGHT);
        roi1.setName("0002-0000-0001");

        Roi roi2 = new Roi(6, 3, ROI_WIDTH, ROI_HEIGHT);
        roi2.setName("0003-0000-0001");

        Roi paddingRoi = new Roi(2, 2, ROI_WIDTH, ROI_HEIGHT);
        paddingRoi.setName("0001-0000-0001");

        Roi rois[] = {paddingRoi, roi1, roi2};

        when(mockRoiManager.getCount()).thenReturn(rois.length);
        when(mockRoiManager.getSliceNumber(anyString())).thenCallRealMethod();
        when(mockRoiManager.getRoisAsArray()).thenReturn(rois);

        ImagePlus image = TestDataMaker.createCuboid(10, 10, 10, TEST_COLOR, 1);

        ImageStack resultStack = RoiUtil.cropStack(mockRoiManager, image.getStack(), false, 0x00, 0);
        assertEquals("Cropped stack has wrong width", 6, resultStack.getWidth());
        assertEquals("Cropped stack has wrong height", 3, resultStack.getHeight());
        assertEquals("Cropped stack has wrong depth", 3, resultStack.getSize());

        int foregroundCount = countColorPixels(resultStack, TEST_COLOR);
        assertEquals("Crop contains wrong part of the original image", TEST_COLOR_COUNT, foregroundCount);

        int backgroundCount = countColorPixels(resultStack, BACKGROUND_COLOR);
        assertEquals("Crop contains wrong part of the original image", BACKGROUND_COLOR_COUNT, backgroundCount);
    }

    private static int countColorPixels(ImageStack stack, int color)
    {
        int count = 0;
        int height = stack.getHeight();
        int width = stack.getWidth();

        for (int z = 1; z <= stack.getSize(); z++) {
            byte pixels[] = (byte[]) stack.getPixels(z);
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    if (pixels[offset + x] == color) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}