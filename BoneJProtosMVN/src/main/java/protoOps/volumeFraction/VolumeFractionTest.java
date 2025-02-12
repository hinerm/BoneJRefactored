package protoOps.volumeFraction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

/**
 * @author Richard Domander
 */
public class VolumeFractionTest {
	private VolumeFraction volumeFraction;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		volumeFraction = new VolumeFraction();
	}

	@Test
	public void testSetImageThrowsNullPointerExceptionIfImageIsNull() throws Exception {
		expectedException.expect(NullPointerException.class);
		expectedException.expectMessage("Must have an input image");

		volumeFraction.setImage(null);
	}

	@Test
	public void testSetImageThrowsIllegalArgumentExceptionIfBitDepthIsWrong() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Input image bit depth must be 8 or 16");
		ImagePlus mockImage = mock(ImagePlus.class);
		when(mockImage.getBitDepth()).thenReturn(24);

		volumeFraction.setImage(mockImage);
	}

	@Test
	public void testSetImageThrowsIllegalArgumentExceptionIfColorImage() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Need a binary or grayscale image");
		ImagePlus mockImage = mock(ImagePlus.class);
		when(mockImage.getBitDepth()).thenReturn(8);
		when(mockImage.getType()).thenReturn(ImagePlus.COLOR_256);

		volumeFraction.setImage(mockImage);
	}

	@Test
	public void testSetVolumeAlgorithmThrowsIllegalArgumentExceptionIfAlgorithmIsInvalid() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No such surface algorithm");

		volumeFraction.setVolumeAlgorithm(-1);
	}

	@Test
	public void testSetSurfaceResamplingThrowsIllegalArgumentExceptionIfValueIsNegative() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Resampling value must be >= 0");

		volumeFraction.setSurfaceResampling(-1);
	}

	@Test
	public void testSetRoiManagerThrowsNullPointerExceptionIfRoiManagerIsNull() throws Exception {
		expectedException.expect(NullPointerException.class);
		expectedException.expectMessage("May not use a null ROI Manager");

		volumeFraction.setRoiManager(null);
	}

	@Test
	public void testSetRoiManagerThrowsIllegalArgumentExceptionIfRoiManagerIsEmpty() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("May not use an empty ROI Manager");
		RoiManager roiManager = mock(RoiManager.class);
		when(roiManager.getCount()).thenReturn(0);

		volumeFraction.setRoiManager(roiManager);
	}
}