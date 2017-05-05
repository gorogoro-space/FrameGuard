package space.gorogoro.frameguard;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * FrameGuard
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2017
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class FrameGuard extends JavaPlugin{
  private FrameGuardDatabase fgdatabase;
  private FrameGuardCommand fgcommand;
  
  /**
   * Get FrameGuardDatabase instance.
   */
  public FrameGuardDatabase getFgDatabase() {
    return fgdatabase;
  }
  
  /**
   * Get FrameGuardCommand instance.
   */
  public FrameGuardCommand getFgCommand() {
    return fgcommand;
  }
  
  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable(){
    try{
      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }
      File configFile = new File(getDataFolder() + "/config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }
      getLogger().log(Level.INFO, getConfig().getString("message-enable"));
      
      // Register event listener.
      PluginManager pm = this.getServer().getPluginManager();
      HandlerList.unregisterAll(this);    // clean up
      pm.registerEvents(new FrameGuardListener(this), this);
      
      // Initialize the database.
      fgdatabase = new FrameGuardDatabase(this);
      fgdatabase.initialize();
      
      // Instance prepared of FrameGuardCommand.
      fgcommand = new FrameGuardCommand(this);
      
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * JavaPlugin method onCommand.
   */
  public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
    try{
      fgcommand.initialize(sender, args);
      if( command.getName().equals("fglock") && sender.hasPermission("frameguard.fglock") ) { 
        return fgcommand.fglock();
      }else if( command.getName().equals("fgunlock") && sender.hasPermission("frameguard.fgunlock")) {
        return fgcommand.fgunlock();
      }else if( command.getName().equals("fginfo") && sender.hasPermission("frameguard.fginfo")) {
        return fgcommand.fginfo();
      }else if( command.getName().equals("fgpurge") && sender.isOp() ) {
        return fgcommand.fgpurge();
      }else if( command.getName().equals("fgreload") && sender.isOp() ) {
        return fgcommand.fgreload();
      }else if( command.getName().equals("fgenable") && sender.isOp() ) {
        return fgcommand.fgenable();
      }else if( command.getName().equals("fgdisable") && sender.isOp() ) {
        return fgcommand.fgdisable();
      }
    }catch(Exception e){
      FrameGuardUtility.logStackTrace(e);
    }finally{
      fgcommand.finalize();
    }
    return false;
  }
  
  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable(){
    try{
      fgdatabase.finalize();
      fgcommand.finalize();
      
      // Unregister all event listener.
      HandlerList.unregisterAll(this);
      
      getLogger().log(Level.INFO, getConfig().getString("message-disable"));
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
}
