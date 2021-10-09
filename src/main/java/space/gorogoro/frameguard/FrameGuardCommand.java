package space.gorogoro.frameguard;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/*
 * FrameGuardCommand
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2017
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class FrameGuardCommand {
  private FrameGuard frameguard;
  protected static final String META_LOCK = "frameguard.fglock";
  protected static final String META_UNLOCK = "frameguard.fgunlock";
  protected static final String META_INFO = "frameguard.fginfo";

  /**
   * Constructor of FrameGuardCommand.
   * @param FrameGuard FrameGuard
   */
  public FrameGuardCommand(FrameGuard frameGuard) {
    try{
      this.frameguard = frameGuard;
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }

  /**
   * Processing of command fglock.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fglock(CommandSender sender, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage(frameguard.getConfig().getString("message-execute-commands-from-chat"));
      return true;
    }
    
    // Set metadata to player.
    Player player = (Player)sender;
    FrameGuardUtility.removeAllPunch(player, frameguard);
    player.setMetadata(META_LOCK, new FixedMetadataValue(frameguard, false));
    sender.sendMessage(frameguard.getConfig().getString("message-please-punch-the-target"));
    return true;
  }
  
  /**
   * Processing of command fgunlock.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fgunlock(CommandSender sender, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage(frameguard.getConfig().getString("message-execute-commands-from-chat"));
      return true;
    }
    
    // Set metadata to player.
    Player player = (Player)sender;
    FrameGuardUtility.removeAllPunch(player, frameguard);
    player.setMetadata(META_UNLOCK, new FixedMetadataValue(frameguard, false));
    sender.sendMessage(frameguard.getConfig().getString("message-please-punch-the-target"));
    return true;
  }

  /**
   * Processing of command fginfo.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fginfo(CommandSender sender, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage(frameguard.getConfig().getString("message-execute-commands-from-chat"));
      return true;
    }
    
    // Set metadata to player.
    Player player = (Player)sender;
    FrameGuardUtility.removeAllPunch(player, frameguard);
    player.setMetadata(META_INFO, new FixedMetadataValue(frameguard, false));
    sender.sendMessage(frameguard.getConfig().getString("message-please-punch-the-target"));
    return true;
  }

  
  /**
   * Processing of command fgpurge.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fgpurge(CommandSender sender, String[] args) {
    if(args.length == 1){
      Integer days = Integer.parseInt(args[0]);
      frameguard.getFgDatabase().purgeData(String.valueOf(days));
      sender.sendMessage(frameguard.getConfig().getString("message-purge-the-data").replace("__DAYS__", String.valueOf(days)));
      return true;
    }
    return false;
  }

  /**
   * Processing of command fgreload.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fgreload(CommandSender sender, String[] args) {
    frameguard.reloadConfig();
    FrameGuardUtility.sendMessage(sender, frameguard.getConfig().getString("message-command-reload"));
    return true;
  }

  /**
   * Processing of command fgenable.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fgenable(CommandSender sender, String[] args) {
    frameguard.onEnable();
    FrameGuardUtility.sendMessage(sender, frameguard.getConfig().getString("message-command-enable"));
    return true;
  }

  /**
   * Processing of command fgdisable.
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean fgdisable(CommandSender sender, String[] args) {
    frameguard.onDisable();
    FrameGuardUtility.sendMessage(sender, frameguard.getConfig().getString("message-command-disable"));
    return true;
  }
}