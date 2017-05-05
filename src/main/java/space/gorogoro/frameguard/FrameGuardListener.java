package space.gorogoro.frameguard;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/*
 * FrameGuardListener
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2017
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class FrameGuardListener implements Listener{
  private FrameGuard frameguard;
  private boolean DEBUG;
  
  /**
   * Constructor of FrameGuardListener.
   */
  public FrameGuardListener(FrameGuard frameGuard) {
    try{
      this.frameguard = frameGuard;
      this.DEBUG = frameguard.getConfig().getBoolean("setting-debug-mode");
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * On block break
   * @param BlockBreakEvent BlockBreakEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockBreak(BlockBreakEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onBlockBreak");
    }
    
    Block block = event.getBlock();
    Location loc = block.getLocation();
    
    if(frameguard.getFgDatabase().isLocked(loc)){
      FrameGuardUtility.sendMessage(event.getPlayer(), frameguard.getConfig().getString("message-block-is-locked"));
      event.setCancelled(true);
      return;
    }
    
    if(frameguard.getFgDatabase().isAttached(loc)){
      FrameGuardUtility.sendMessage(event.getPlayer(), frameguard.getConfig().getString("message-block-has-locked-wall-hanging"));
      event.setCancelled(true);
      return;
    }
  }
  
  /**
   * On block burn
   * @param BlockBurnEvent BlockBurnEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockBurn(BlockBurnEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: BlockBurn");
    }
    
    Block block = event.getBlock();
    if(!FrameGuardUtility.isContinue(block)){
      return;
    }
    
    if(frameguard.getFgDatabase().isLocked(block.getLocation())){
      frameguard.getLogger().log(Level.INFO, frameguard.getConfig().getString("message-block-is-locked"));
      event.setCancelled(true);
      return;
    }
  }
  
  /**
   * On block fade
   * @param BlockFadeEvent BlockFadeEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockFade(BlockFadeEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onBlockFade");
    }
    
    Block block = event.getBlock();
    if(!FrameGuardUtility.isContinue(block)){
      return;
    }
    
    if(frameguard.getFgDatabase().isLocked(block.getLocation())){
      event.setCancelled(true);
      return;
    }
  }
  
  /**
   * On block pistion extend
   * @param BlockPistonExtendEvent BlockPistonExtendEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onBlockPistonExtend");
    }
    
    // Cancel the event if the piston has stretched and there is a lock at the end
    Location extLoc = event.getBlock().getRelative(event.getDirection()).getLocation();
    if (  frameguard.getFgDatabase().isLocked(extLoc) ) {
        event.setCancelled(true);
        return;
    }
    for ( Block block : event.getBlocks() ) {
        // Confirm lock of moving block
        for ( BlockFace blockFace : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST} ) {
          if(frameguard.getFgDatabase().isLocked(block.getRelative(blockFace).getLocation())){
            event.setCancelled(true);
            return;
          }
        }
        
        // Confirm the lock of the move destination
        Block attachedBlock = FrameGuardUtility.getAttachedBlockByBlock(block);
        if(attachedBlock != null){
          Location attachedBlockLoc = attachedBlock.getLocation();
          if(frameguard.getFgDatabase().isLocked(attachedBlockLoc)){
            event.setCancelled(true);
            return;
          }
        }
    }
  }
  
  /**
   * On block piston retract
   * @param BlockPistonRetractEvent BlockPistonRetractEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onBlockPistonRetract");
    }
    
    Block block = event.getBlock().getRelative(event.getDirection());
    if ( block == null || block.isEmpty() || block.isLiquid() ) {
      return;
    }
    for ( BlockFace blockFace : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST} ) {
      if(frameguard.getFgDatabase().isLocked(block.getRelative(blockFace).getLocation())){
        event.setCancelled(true);
        return;
      }
    }
  }
  
  /**
   * On block place
   * @param BlockPlaceEvent BlockPlaceEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onBlockPlace(BlockPlaceEvent event){
    if(DEBUG) {
      frameguard.getLogger().info("Called: onBlockPlace");
    }
    
    Block block = event.getBlock();
    if(!FrameGuardUtility.isContinue(block)){
      return;
    }
    
    if(frameguard.getFgDatabase().isLocked(block.getLocation())){
      frameguard.getLogger().log(Level.INFO, frameguard.getConfig().getString("message-block-is-locked"));
      event.setCancelled(true);
      return;
    }
  }
  
  /**
   * On entity damage by entity
   * @param EntityDamageByEntityEvent EntityDamageByEntityEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onEntityDamageByEntity");
    }
    
    Entity entity = event.getEntity();
    if(!FrameGuardUtility.isContinue(entity)){
      return;
    }
    
    Location loc = entity.getLocation();
    if(frameguard.getFgDatabase().isLocked(loc)){
      if(event.getDamager().getType() == EntityType.PLAYER){
        Player damager = (Player)event.getDamager();
        
        if (FrameGuardUtility.isInPunch(damager)){
          event.setCancelled(true);
          frameguard.getFgDatabase().finishPunch(damager, event.getEntity());
          return;
        }
        
        if(!frameguard.getFgDatabase().isLockedOwner(loc, damager)){
          // Cancel if broken by non-owner
          frameguard.getFgDatabase().informationLockData(damager, entity);
          event.setCancelled(true);
          return;
        }
      } else {
        // Cancel if broken by arrow
        event.setCancelled(true);
      }
    } else {
      if(event.getDamager().getType() == EntityType.PLAYER){
        Player damager = (Player)event.getDamager();
        
        if (FrameGuardUtility.isInPunch(damager)){
          event.setCancelled(true);
          frameguard.getFgDatabase().finishPunch(damager, event.getEntity());
          return;
        }
      }
    }
  }
  
  /**
   * On hanging break
   * @param HangingBreakEvent HangingBreakEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onHangingBreak(HangingBreakEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onHangingBreak");
    }
    
    Hanging hanging = event.getEntity();
    if(!FrameGuardUtility.isContinue(hanging)){
      return;
    }
    
    if( frameguard.getFgDatabase().isLocked(hanging.getLocation()) ){
      
      if(hanging.getLocation().getBlock().getType() != Material.AIR){
        hanging.getLocation().getBlock().setType(Material.AIR);
      }
      
      Location loc = frameguard.getFgDatabase().getAttachedLocation(hanging.getLocation());
      Material attachedMaterial = frameguard.getFgDatabase().getAttachedMaterial(hanging.getLocation());
      Block restoredBlock = loc.getBlock();
      if(restoredBlock.getType() != attachedMaterial) {
        restoredBlock.setType(attachedMaterial);
      }
      
      String blockFace = frameguard.getFgDatabase().getBlockFace(hanging.getLocation());
      if(blockFace.equals(hanging.getFacing().name())) {
        event.setCancelled(true);
      }
    }
  }
  
  /**
   * On hanging break by entity
   * @param HangingBreakByEntityEvent HangingBreakByEntityEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onHangingBreakByEntity");
    }
    
    Hanging hanging = event.getEntity();
    if(!FrameGuardUtility.isContinue(hanging)){
      return;
    }
    
    if ( event.getRemover().getType() == EntityType.PLAYER ) {
      Player damager = (Player)event.getRemover();
      if (FrameGuardUtility.isInPunch(damager)){
        event.setCancelled(true);
        frameguard.getFgDatabase().finishPunch(damager, event.getEntity());
        return;
      }
    }
    
    if( frameguard.getFgDatabase().isLocked(hanging.getLocation()) ){
      event.setCancelled(true);
      if ( event.getRemover().getType() == EntityType.PLAYER ) {
        Player damager = (Player)event.getRemover();
        frameguard.getFgDatabase().informationLockData(damager, hanging);
      }
    }
  }
  
  /**
   * On hanging place
   * @param HangingPlaceEvent HangingPlaceEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onHangingPlace(HangingPlaceEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onHangingPlace");
    }
    
    Hanging hanging = event.getEntity();
    if(!FrameGuardUtility.isContinue(hanging)){
      return;
    }
    
    if(frameguard.getFgDatabase().isLocked(hanging.getLocation())){
      if(!frameguard.getFgDatabase().isLockedOwner(hanging.getLocation(), event.getPlayer())){
        FrameGuardUtility.sendMessage(event.getPlayer(), frameguard.getConfig().getString("message-block-is-locked"));
        event.setCancelled(true);
      }
      return;
    }else{
      Player player = event.getPlayer();
      if (FrameGuardUtility.isInPunch(player)){
        event.setCancelled(true);
        frameguard.getFgDatabase().finishPunch(player, event.getEntity());
        return;
      }
    }
  }
  
  /**
   * On player interact entity
   * @param PlayerInteractEntityEvent PlayerInteractEntityEvent
   */
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if(DEBUG) {
      frameguard.getLogger().info("Called: onPlayerInteractEntity");
    }
    
    Entity entity = event.getRightClicked();
    if(!FrameGuardUtility.isContinue(entity)){
      return;
    }
    
    if(frameguard.getFgDatabase().isLocked(entity.getLocation())){
      if(!frameguard.getFgDatabase().isLockedOwner(entity.getLocation(), event.getPlayer())){
        FrameGuardUtility.sendMessage(event.getPlayer(), frameguard.getConfig().getString("message-block-is-locked"));
        event.setCancelled(true);
      }
      return;
    }
  }
  
}