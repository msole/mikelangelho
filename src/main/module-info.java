import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.artPipeline.generators.ImageGenerator;
import com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveFileType;
import com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;
import com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;

module com.marginallyclever.makelangelo {
	requires java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires jrpicam;
	requires jogamp.fat;
	requires kabeja;
	requires org.slf4j;
	requires jssc;
	requires vecmath;
	requires batik.all;
	requires xml.apis.ext;
	requires junit;
	
	uses MakelangeloHardwareProperties;
	provides MakelangeloHardwareProperties with 
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.CartesianProperties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo2Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo3_3Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo3Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo5Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloCustomProperties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.ZarplotterProperties;
	
	uses LoadAndSaveFileType;
	provides LoadAndSaveFileType with
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveDXF,
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveGCode,
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveImage,
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveScratch2,
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveScratch3,
		com.marginallyclever.artPipeline.loadAndSave.LoadAndSaveSVG;

	uses ImageConverter;
	provides ImageConverter with
		com.marginallyclever.artPipeline.converters.Converter_Boxes,
		com.marginallyclever.artPipeline.converters.Converter_CMYK,
		com.marginallyclever.artPipeline.converters.Converter_Crosshatch,
		com.marginallyclever.artPipeline.converters.Converter_Moire,
		com.marginallyclever.artPipeline.converters.Converter_Multipass,
		com.marginallyclever.artPipeline.converters.Converter_Pulse,
		com.marginallyclever.artPipeline.converters.Converter_RandomLines,
		com.marginallyclever.artPipeline.converters.Converter_Sandy,
		com.marginallyclever.artPipeline.converters.Converter_Spiral,
		com.marginallyclever.artPipeline.converters.Converter_Spiral_CMYK,
		com.marginallyclever.artPipeline.converters.Converter_SpiralPulse,
		com.marginallyclever.artPipeline.converters.Converter_VoronoiStippling,
		com.marginallyclever.artPipeline.converters.Converter_VoronoiZigZag,
		com.marginallyclever.artPipeline.converters.Converter_Wander;

	uses ImageGenerator;
	provides ImageGenerator with
		com.marginallyclever.artPipeline.generators.Generator_Border,
		com.marginallyclever.artPipeline.generators.Generator_Dragon,
		com.marginallyclever.artPipeline.generators.Generator_FibonacciSpiral,
		com.marginallyclever.artPipeline.generators.Generator_FillPage,
		com.marginallyclever.artPipeline.generators.Generator_GosperCurve,
		com.marginallyclever.artPipeline.generators.Generator_GraphPaper,
		com.marginallyclever.artPipeline.generators.Generator_HilbertCurve,
		com.marginallyclever.artPipeline.generators.Generator_KochCurve,
		com.marginallyclever.artPipeline.generators.Generator_Lissajous,
		com.marginallyclever.artPipeline.generators.Generator_LSystemTree,
		com.marginallyclever.artPipeline.generators.Generator_Maze,
		com.marginallyclever.artPipeline.generators.Generator_Package,
		com.marginallyclever.artPipeline.generators.Generator_Polyeder,
		com.marginallyclever.artPipeline.generators.Generator_SierpinskiTriangle,
		com.marginallyclever.artPipeline.generators.Generator_Spirograph,
		com.marginallyclever.artPipeline.generators.Generator_Text;

	uses MachineStyle;
	provides MachineStyle with
		com.marginallyclever.makelangeloRobot.machineStyles.CoreXY,
		com.marginallyclever.makelangeloRobot.machineStyles.Cartesian,
		com.marginallyclever.makelangeloRobot.machineStyles.Polargraph,
		com.marginallyclever.makelangeloRobot.machineStyles.Zarplotter;
}