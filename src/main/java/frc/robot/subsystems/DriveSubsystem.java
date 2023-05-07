//----------------------[Package]----------------------//
package frc.robot.subsystems;
//----------------------[Library]----------------------//
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Timer;
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import java.util.function.DoubleSupplier;
import java.util.Objects;
import frc.robot.Constants.Drive.HardwareInformation;
import frc.robot.Constants.Drive.MotorPorts;
import frc.robot.Constants.Drive.Data.PID;
import frc.robot.Constants.Functions;
//------------------[Drive Subsystem]------------------//
/*
 * To-do list
 * TODO: Add Simulator support.
 * TODO: Obtain drivetrain measurements
 * TODO: Obtain drivetain wheel measurements
 * TODO: Obtain drivetrain PIDS * 
 */
/**
 * Magnus' drivetrain subsystem
 */
public class DriveSubsystem extends SubsystemBase 
{
    //-------------------[Constants]-------------------//
    private final WPI_TalonFX FRONT_LEFT; private final WPI_TalonFX FRONT_RIGHT;
    private final WPI_TalonFX REAR_LEFT; private final WPI_TalonFX REAR_RIGHT; 
    private final MotorControllerGroup DB_LEFT; private final MotorControllerGroup DB_RIGHT;
    private final PIDController DB_M_PID; private final DifferentialDrive DB_DRIVEBASE;  
    private final DifferentialDriveKinematics DB_KINEMATICS; private final DifferentialDriveWheelSpeeds DB_WHEEL_SPEEDS; 
    private final Timer DB_PERIODIC_TIME;
    //-------------------[Properties]------------------//   
    private Rotation2d DB_Heading; private Boolean DB_Mode; private ChassisSpeeds DB_Chassis_Speeds;
    private Double DB_Speed_Coefficient; private Class<?> DB_Driver;
    //------------------[Constructors]-----------------//
    /**
     * Constructor.
     * @param Driver - Driver Class Profile
     */
    public DriveSubsystem(Class<?> Driver)
    {
        FRONT_LEFT = new WPI_TalonFX(MotorPorts.FL); FRONT_RIGHT = new WPI_TalonFX(MotorPorts.FR);
        REAR_LEFT = new WPI_TalonFX(MotorPorts.RL); REAR_RIGHT = new WPI_TalonFX(MotorPorts.RR);
        DB_LEFT = new MotorControllerGroup(FRONT_LEFT,REAR_LEFT); DB_RIGHT = new MotorControllerGroup(FRONT_RIGHT,REAR_RIGHT);
        DB_M_PID = new PIDController(PID.DB_KP,PID.DB_KI,PID.DB_KP);
        DB_DRIVEBASE = new DifferentialDrive(DB_LEFT, DB_RIGHT); DB_KINEMATICS = new DifferentialDriveKinematics(HardwareInformation.DIMENSIONS[0]);
        DB_WHEEL_SPEEDS = new DifferentialDriveWheelSpeeds(0.0,0.0); DB_PERIODIC_TIME = new Timer();
        DB_Heading = new Rotation2d(0.0); DB_Mode = false; DB_Speed_Coefficient = 1.0; DB_Driver = Driver;
    }
    /**
     * Constructor.
     * @param FL - Front left motorcontroller port
     * @param FR - Front right motorcontroller port
     * @param RL - Rear left motorcontroller port
     * @param RR - Rear right motorcontroller port
     * @param Driver - Driver Class Profile
     */
    public DriveSubsystem(Integer FL, Integer FR, Integer RL, Integer RR, Class<?> Driver)
    {
        Objects.requireNonNull(FL,"Front left motor controller port cannot be Null"); Objects.requireNonNull(FR,"Front right motor controller port Cannot be Null");
        Objects.requireNonNull(RL,"Rear left motor controller port Cannot be Null"); Objects.requireNonNull(RR,"Rear right motor controller port Cannot be Null");
        FRONT_LEFT = new WPI_TalonFX(FL); FRONT_RIGHT = new WPI_TalonFX(FR); 
        REAR_LEFT = new WPI_TalonFX(RL); REAR_RIGHT = new WPI_TalonFX(RR);
        DB_LEFT = new MotorControllerGroup(FRONT_LEFT,REAR_LEFT); DB_RIGHT = new MotorControllerGroup(FRONT_RIGHT,REAR_RIGHT);
        DB_M_PID = new PIDController(PID.DB_KP,PID.DB_KI,PID.DB_KP); DB_DRIVEBASE = new DifferentialDrive(DB_LEFT, DB_RIGHT); 
        DB_KINEMATICS = new DifferentialDriveKinematics(HardwareInformation.DIMENSIONS[0]); DB_WHEEL_SPEEDS = new DifferentialDriveWheelSpeeds(0.0,0.0);
        DB_PERIODIC_TIME = new Timer(); DB_Heading = new Rotation2d(0.0); DB_Mode = false; DB_Speed_Coefficient = 1.0; DB_Driver = Driver;
    }
    //-----------------[Drive Control]-----------------//
    /**
     * Basic arcade drive control
     * @param Velocity - Velocity Target
     * @param Rotation - Rotation Target
     */
    public void arcadeDrive(Double Velocity, Double Rotation) 
    {   
        DB_DRIVEBASE.arcadeDrive((DB_Mode)? (-Velocity * 0.20): (-Velocity * DB_Speed_Coefficient),(DB_Mode)? (-Rotation * 0.20): (-Rotation));
        /*DB_DRIVEBASE.arcadeDrive((DB_Mode)? (-Velocity * 0.20): (-DB_M_PID.calculate(Functions.norm(DB_KINEMATICS.toChassisSpeeds(DB_WHEEL_SPEEDS).vxMetersPerSecond,
        DB_KINEMATICS.toChassisSpeeds(DB_WHEEL_SPEEDS).vyMetersPerSecond),Velocity * DB_Speed_Coefficient)),(DB_Mode)? (-Rotation * 0.20): (-Rotation)); */
    }
    /**
     * Basic arcade drive control
     * @param Velocity - Velocity Target
     * @param Rotation - Rotation Target
     */
    public void arcadeDrive(DoubleSupplier Velocity, DoubleSupplier Rotation) {arcadeDrive(Velocity.getAsDouble(), Rotation.getAsDouble());}
    /**
     * Pose2d arcade drive control
     * @param Demand - Pose2d Target with both a target Velocity and Rotation
     */
    public void arcadeDrive(Pose2d Demand) {arcadeDrive(Functions.norm(Demand.getX(),Demand.getY()), Demand.getRotation().getDegrees());}
    /** Configure Magnus' drivebase devices */
    public void configureDrivebase()
    {
        FRONT_LEFT.configFactoryDefault(); FRONT_RIGHT.configFactoryDefault();
        REAR_LEFT.configFactoryDefault(); REAR_RIGHT.configFactoryDefault();
        REAR_LEFT.follow(FRONT_LEFT); REAR_RIGHT.follow(FRONT_RIGHT);        
        FRONT_LEFT.setNeutralMode(NeutralMode.Brake); FRONT_RIGHT.setNeutralMode(NeutralMode.Brake);
        REAR_LEFT.setNeutralMode(NeutralMode.Brake); REAR_LEFT.setNeutralMode(NeutralMode.Brake);
        FRONT_LEFT.setInverted(TalonFXInvertType.Clockwise); FRONT_RIGHT.setInverted(TalonFXInvertType.CounterClockwise);
        REAR_LEFT.setInverted(TalonFXInvertType.Clockwise); REAR_RIGHT.setInverted(TalonFXInvertType.CounterClockwise); 
    }
    /** Toggle the current driving mode */
    public void toggleDrivingMode() {this.DB_Mode = !this.DB_Mode;}
    /** Set a new driving mode 
     * @param Mode - Desired driving mode */
    public void setDrivingMode(Boolean Mode) {this.DB_Mode = Mode;}
    /** Decrement driving speed coefficient */
    public void decrementCoefficient()
    {DB_Speed_Coefficient -= (Double)Functions.getFieldValue(DB_Driver, "SPEED_COEFFICIENT_SENSITIVITY");}
    /** Increment driving speed coefficient */
    public void incrementCoefficient()
    {DB_Speed_Coefficient += (Double)Functions.getFieldValue(DB_Driver, "SPEED_COEFFICIENT_SENSITIVITY");}
    //-------------------[Subsystem]-------------------//
    @Override
    public void periodic()
    {
        DB_WHEEL_SPEEDS.leftMetersPerSecond = Math.max(FRONT_LEFT.getSelectedSensorVelocity(),REAR_LEFT.getSelectedSensorVelocity()) * HardwareInformation.ENCODER_TICK_TO_METER_FACTOR;
        DB_WHEEL_SPEEDS.rightMetersPerSecond = Math.max(FRONT_LEFT.getSelectedSensorVelocity(),FRONT_RIGHT.getSelectedSensorVelocity()) * HardwareInformation.ENCODER_TICK_TO_METER_FACTOR;
        DB_Chassis_Speeds = DB_KINEMATICS.toChassisSpeeds(DB_WHEEL_SPEEDS);
        var DB_Heading_Current  = ((DB_Chassis_Speeds.omegaRadiansPerSecond * DB_PERIODIC_TIME.get()) + (DB_Heading.getRadians()));
        DB_Heading = new Rotation2d((DB_Heading_Current > 360)? ((DB_Heading_Current - 360)): (DB_KINEMATICS.toChassisSpeeds(DB_WHEEL_SPEEDS).omegaRadiansPerSecond
         + (DB_Heading.getRadians())));
    }
    @Override
    public void simulationPeriodic() {}
    //-------------------[Accessors]-------------------//
    /** @return Differential drive left side velocity  */
    public Double getLeftVelocity()
    {return DB_WHEEL_SPEEDS.leftMetersPerSecond;}
    /** @return Differential drive right side velocity  */
    public Double getRightVelocity()
    {return DB_WHEEL_SPEEDS.rightMetersPerSecond;}
    //-------------------[Mutators]--------------------// 
}

