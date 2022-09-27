package meshIneBits.gui.view2d;

public class MousePositionPanel extends PropertyPanel  {
private static final String XPOSITION="X";
    private static final String YPOSITION="Y";

    MousePositionPanel(String title) {
        super(title);
    initTable(getPropertiesOf(0,0));
    }

    @Override
    public void updateProperties(Object object) {

    }


    /*
        private static String[][] getPropertiesOf(Mesh mesh) {
            if (mesh == null) {
                return new String[][]{
                        {XPOSITION, "UNKNOWN"},
                        {YPOSITION, "UNKNOWN"}
                };
            } else {
                File modelFile = new File(mesh.getModelFile());
                Model model = mesh.getModel();
                return new String[][]{
                        {XPOSITION, String.valueOf(getPointerInfo().getLocation())},
                        {YPOSITION, modelFile.getPath()}
                };
            }

        }
    */
    public static String[][]getPropertiesOf(double x,double y){

        return new String[][]{
                {XPOSITION, String.valueOf(x)},
                {YPOSITION,  String.valueOf(y)}
        };

    }





     void updateProperties(double x,double y ) {
        updateProperty(XPOSITION, String.valueOf(x));
         updateProperty(YPOSITION, String.valueOf(y));
    }



}
