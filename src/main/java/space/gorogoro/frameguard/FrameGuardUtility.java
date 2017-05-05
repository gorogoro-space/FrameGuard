package space.gorogoro.frameguard;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;

/*
 * FrameGuardUtility
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2017
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class FrameGuardUtility {
  
  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  public static void logStackTrace(Exception e){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      Bukkit.getLogger().log(Level.WARNING, sw.toString());
  }
  
  /**
   * Determine whether processing can be continued.
   * @param Hanging Hanging
   * @return boolean true:Continue false:Stop
   */
  public static boolean isContinue(Hanging hanging){
    if(hanging.getType() == EntityType.ITEM_FRAME){
      return true;
    }else if(hanging.getType() == EntityType.PAINTING){
      return true;
    }
    return false;
  }
  
  /**
   * Determine whether processing can be continued.
   * @param Entity Entity
   * @return boolean true:Continue false:Stop
   */
  public static boolean isContinue(Entity entity){
    if ( entity.getType() == EntityType.ITEM_FRAME ) {
      return true;
    }else if ( entity.getType() == EntityType.PAINTING ) {
      return true;
    }
    return false;
  }
  
  /**
   * Determine whether processing can be continued.
   * @param Block Block
   * @return boolean true:Continue false:Stop
   */
  public static boolean isContinue(Block block) {
    if ( block != null && !block.isEmpty() && !block.isLiquid() ) {
      for (BlockFace blockface : new BlockFace[] {
          BlockFace.NORTH,
          BlockFace.SOUTH,
          BlockFace.EAST,
          BlockFace.WEST,
      }) {
        Block relative = block.getRelative(blockface);
        if (relative.getType() == Material.ITEM_FRAME) {
          return true;
        } else if (relative.getType() == Material.ITEM_FRAME) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Determine whether punch is being processed.
   * @param Player Player
   * @return boolean true:That's right false:That's not it
   */
  public static boolean isInPunch(Player player){
    if( player.hasMetadata(FrameGuardCommand.META_LOCK)){
      return true;
    }else if( player.hasMetadata(FrameGuardCommand.META_UNLOCK)){
      return true;
    }else if( player.hasMetadata(FrameGuardCommand.META_INFO)){
      return true;
    }
    return false;
  }
  
  /**
   * Remove punch processing.
   * @param Player Player
   * @param FrameGuard FrameGuard
   */
  public static void removeAllPunch(Player player, FrameGuard frameguard){
    player.removeMetadata(FrameGuardCommand.META_LOCK, frameguard);
    player.removeMetadata(FrameGuardCommand.META_UNLOCK, frameguard);
    player.removeMetadata(FrameGuardCommand.META_INFO, frameguard);
  }
  
  /**
   * Send message to player
   * @param CommandSender CommandSender
   * @param String message
   */
  public static void sendMessage(CommandSender sender, String message){
    sender.sendMessage((Object)ChatColor.DARK_RED + "[FrameGuard]" + " " + (Object)ChatColor.RED + message);
  }
  
  /**
   * Get attached block by hanging
   * @param Hanging Hanging
   * @return Block Attached block
   */
  public static Block getAttachedBlockByHanging(Hanging hanging){
    // Calculate the position of the wall hanging block
    Location attachedLocation = new Location(
      hanging.getLocation().getWorld(),
      hanging.getLocation().getX() + hanging.getAttachedFace().getModX(),
      hanging.getLocation().getY() + hanging.getAttachedFace().getModY(),
      hanging.getLocation().getZ() + hanging.getAttachedFace().getModZ()
    );
    return attachedLocation.getBlock();
  }
  
  /**
   * Get attached block by block
   * @param Block Break block
   * @return Block Attached block
   */
  public static Block getAttachedBlockByBlock(Block breakBlock){
    if ( breakBlock != null && !breakBlock.isEmpty() && !breakBlock.isLiquid() ) {
      // Calculate the position of the wall hanging block
      for (BlockFace blockface : new BlockFace[] {
          BlockFace.NORTH,
          BlockFace.SOUTH,
          BlockFace.EAST,
          BlockFace.WEST,
      }) {
        Block faceBlock = breakBlock.getRelative(blockface);
        Location faceLoc = faceBlock.getLocation();
        ItemFrame im = getItemFrame(faceLoc);
        // When there was a painting sticking to a broken block
        if (im != null) {
          // When the direction in which the painting sticks and the direction in which it was broken coincided
          if(im.getFacing().name().equals(blockface.name())){
            return faceBlock;
          }
        }
        
        Painting p = getPainting(faceLoc);
        // When there was a painting sticking to a broken block
        if (p != null) {
          // When the direction in which the painting sticks and the direction in which it was broken coincided
          if(p.getFacing().name().equals(blockface.name())){
            return faceBlock;
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Get ItemFrame by location.
   * @param Location Location
   * @return ItemFrame ItemFrame
   */
  public static ItemFrame getItemFrame(Location loc) {
    for (Entity e : loc.getChunk().getEntities()){
      if (e.getType() == EntityType.ITEM_FRAME){
        if (e.getLocation().getBlock().getLocation().distance(loc) == 0) {
          return (ItemFrame) e;
        }
      }
    }
    return null;
  }
  
  /**
   * Get Painting by location.
   * @param Location Location
   * @return Painting Painting
   */
  public static Painting getPainting(Location loc) {
    for (Entity e : loc.getChunk().getEntities()){
      if (e.getType() == EntityType.PAINTING){
        if (e.getLocation().getBlock().getLocation().distance(loc) == 0) {
          return (Painting) e;
        }
      }
    }
    return null;
  }
  
}