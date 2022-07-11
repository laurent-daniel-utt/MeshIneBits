
package meshIneBits.opcuaHelper;

import meshIneBits.util.CustomLogger;

import java.util.concurrent.ExecutionException;

public class DepositingMachineOPCUAHelper extends BitSLickrMachineAdapter {

    private static final CustomLogger logger = new CustomLogger(DepositingMachineOPCUAHelper.class);

    public final String start_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.start_button";
    public final String stop_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.stop_button";
    public final String reset_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.reset_button";
    public final String continue_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_button";
    public final String pause_depose = "|var|CPX-E-CEC-M1-PN.Application.visu.pause_button";
    public final String continue_after_turn_off = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_turn_off_button";
    public final String continue_after_E_STOP = "|var|CPX-E-CEC-M1-PN.Application.visu.continue_after_E_STOP_button";
    public final String acknowledge_error = "|var|CPX-E-CEC-M1-PN.Application.visu.acknowledge_error_button";
    public final String message_error = "|var|CPX-E-CEC-M1-PN.Application.GDV.message_error";
    public final String lock_continue_button="|var|CPX-E-CEC-M1-PN.Application.visu.lock_continue_button";
    public final String x_current_position = "|var|CPX-E-CEC-M1-PN.Application.Synchronization_X.T1_Position";
    public final String z_current_position = "|var|CPX-E-CEC-M1-PN.Application.Synchronization_Z.T3_Position";
    public final String y_current_position = "|var|CPX-E-CEC-M1-PN.Application.Control_Y.T5_Position";
    public final String subx_current_position = "|var|CPX-E-CEC-M1-PN.Application.Control_SubX.T6_Position";
    public final String subz_current_position = "|var|CPX-E-CEC-M1-PN.Application.Control_SubZ.T7_Position";
    public final String theta_current_position = "|var|CPX-E-CEC-M1-PN.Application.Control_Theta.T8_Position";
    public final String current_bit_id = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.id";
    public final String current_bit_id_in_batch = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.id_in_batch";
    public final String current_bit_x = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.x";
    public final String current_bit_z = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.z";
    public final String current_bit_y = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.y";
    public final String current_bit_subx = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.subx";
    public final String current_bit_rotation = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.rotation";
    public final String current_bit_refline_vu = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.refline_vu";
    public final String current_bit_refline_rot = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.refline_rot";
    public final String current_bit_theta = "|var|CPX-E-CEC-M1-PN.Application.GDV.current_bit.theta";
    public final String camera_login = "|var|CPX-E-CEC-M1-PN.Application.visu.camera_login_button";
    public final String camera_capture_image = "|var|CPX-E-CEC-M1-PN.Application.visu.camera_capture_image_button";
    public final String reset_power = "|var|CPX-E-CEC-M1-PN.Application.visu.reset_power_button";
    public final String take_batch = "|var|CPX-E-CEC-M1-PN.Application.visu.take_batch_button";
    public final String depose_batch = "|var|CPX-E-CEC-M1-PN.Application.visu.depose_batch_button";
    public final String read_xml_file = "|var|CPX-E-CEC-M1-PN.Application.visu.read_xml_file_button";
    public final String rename_xml_file = "|var|CPX-E-CEC-M1-PN.Application.visu.rename_xml_file_button";
    public final String synchro_axes_x = "|var|CPX-E-CEC-M1-PN.Application.visu.synchro_X_button";
    public final String synchro_axes_z = "|var|CPX-E-CEC-M1-PN.Application.visu.synchro_Z_button";
    public final String homing_axis_subx = "|var|CPX-E-CEC-M1-PN.Application.visu.homing_subx_button";
    public final String homing_axis_subz = "|var|CPX-E-CEC-M1-PN.Application.visu.homing_subz_button";
    public final String homing_axis_theta = "|var|CPX-E-CEC-M1-PN.Application.visu.homing_theta_button";


    public ICustomResponse startDepose() {
        try {
            return writeVariableNode(start_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse stopDepose() {
        try {
            return writeVariableNode(stop_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse resetDepose() {
        try {
            return writeVariableNode(reset_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueDepose() {
        try {
            return writeVariableNode(continue_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse pauseDepose() {
        try {
            return writeVariableNode(pause_depose, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueAfterTurnOff() {
        try {
            return writeVariableNode(continue_after_turn_off, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse continueAfterESTOP() {
        try {
            return writeVariableNode(continue_after_E_STOP, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse acknowledgeError() {
        try {
            return writeVariableNode(acknowledge_error, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getMessageError(){
        try {
            return readVariableNode(message_error);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getLockContinueButton(){
        try {
            return readVariableNode(lock_continue_button);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getXCurrentPosition(){
        try {
            return readVariableNode(x_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getZCurrentPosition(){
        try {
            return readVariableNode(z_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getYCurrentPosition(){
        try {
            return readVariableNode(y_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getSubXCurrentPosition(){
        try {
            return readVariableNode(subx_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getSubZCurrentPosition(){
        try {
            return readVariableNode(subz_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getThetaCurrentPosition(){
        try {
            return readVariableNode(theta_current_position);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitId(){
        try {
            return readVariableNode(current_bit_id);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitIdInBatch(){
        try {
            return readVariableNode(current_bit_id_in_batch);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitX(){
        try {
            return readVariableNode(current_bit_x);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitZ(){
        try {
            return readVariableNode(current_bit_z);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitY(){
        try {
            return readVariableNode(current_bit_y);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitSubX(){
        try {
            return readVariableNode(current_bit_subx);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitRotation(){
        try {
            return readVariableNode(current_bit_rotation);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitReflineVu(){
        try {
            return readVariableNode(current_bit_refline_vu);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitReflineRot(){
        try {
            return readVariableNode(current_bit_refline_rot);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse getCurrentBitTheta(){
        try {
            return readVariableNode(current_bit_theta);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse cameraLogin() {
        try {
            return writeVariableNode(camera_login, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse cameraCaptureImage() {
        try {
            return writeVariableNode(camera_capture_image, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse resetPowerAxes() {
        try {
            return writeVariableNode(reset_power, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse takeBatch() {
        try {
            return writeVariableNode(take_batch, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse deposeBatch() {
        try {
            return writeVariableNode(depose_batch, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse readXMLFile() {
        try {
            return writeVariableNode(read_xml_file, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse renameXMLFile() {
        try {
            return writeVariableNode(rename_xml_file, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse synchroAxesX() {
        try {
            return writeVariableNode(synchro_axes_x, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse synchroAxesZ() {
        try {
            return writeVariableNode(synchro_axes_z, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse homingAxisSubX() {
        try {
            return writeVariableNode(homing_axis_subx, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse homingAxisSubZ() {
        try {
            return writeVariableNode(homing_axis_subz, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse homingAxisTheta() {
        try {
            return writeVariableNode(homing_axis_theta, "Boolean", true);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }







/*
    public ICustomResponse setPosition(double p) {
        try {
            return writeVariableNode(deposePosition, "short", p);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    public ICustomResponse setVitesse(short v) {
        try {
            return writeVariableNode(deposeVitesse, "short", v);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
*/

    @Override
    public String getEndpointUrl() {
        return BitSLicRHelperConfig.depose_machine_url;
    }
}

