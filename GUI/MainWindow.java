package GUI;

import GUI.ColorPicker.AlphaSlider;
import GUI.ColorPicker.HSLWheel;
import GUI.ColorPicker.LightnessSlider;
import com.trolltech.qt.gui.*;

public class MainWindow extends QMainWindow{
    private Signals signals = Signals.getInstance();
    public final NavigationButtons navigationButtons;
    public final MoveNavigator moveNavigator;
    public final QWidget colorSetter;

    public MainWindow(String objectName) {
        super();
        setObjectName(objectName);

        navigationButtons = new NavigationButtons(this, objectName() + ".navigationButtons");
        moveNavigator = new MoveNavigator(this, objectName() + ".moveNavigator");
        colorSetter = new QWidget(this);
        colorSetter.setObjectName(objectName() + ".colorSetter");
        setupColorPicker();

        setCentralWidget(new CentralWidget(this, objectName() + ".centralWidget"));
    }

    private void setupColorPicker(){
        colorSetter.setLayout(new QHBoxLayout());

        HSLWheel colorWheel = new HSLWheel(colorSetter);
        AlphaSlider alphaSlider = new AlphaSlider(colorSetter);
        LightnessSlider lightnessSlider = new LightnessSlider(colorSetter);

        colorWheel.colorSelected.connect(alphaSlider, "setBaseColor(QColor)");
        colorWheel.colorSelected.connect(lightnessSlider, "setBaseColor(QColor)");
        alphaSlider.colorSelected.connect(colorWheel, "setAlpha(QColor)");
        lightnessSlider.colorSelected.connect(colorWheel, "setLightness(QColor)");

        colorSetter.layout().addWidget(colorWheel);
        colorSetter.layout().addWidget(lightnessSlider);
        colorSetter.layout().addWidget(alphaSlider);
    }

    @Override
    protected void closeEvent(QCloseEvent event) {
        signals.exitApplication.emit();
        super.closeEvent(event);
    }
}
