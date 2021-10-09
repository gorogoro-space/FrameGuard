package space.gorogoro.frameguard;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;

/*
 * FrameGuardDatabase
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2017
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class FrameGuardDatabase {
  private FrameGuard frameguard;
  private Connection con;

  /**
   * Constructor of FrameGuardDatabase.
   * @param FrameGuard FrameGuard
   */
  public FrameGuardDatabase(FrameGuard frameGuard) {
    this.frameguard = frameGuard;
  }

  /**
   * Get connection.
   * @return Connection Connection
   */
  private Connection getCon(){
    try{
      // Create database folder.
      if(!frameguard.getDataFolder().exists()){
        frameguard.getDataFolder().mkdir();
      }
      if(con == null) {
        // Select JDBC driver.
        Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:" + frameguard.getDataFolder() + File.separator + "database.db");
        con.setAutoCommit(true);
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
      closeCon(con);
    }
    return con;
  }
  
  /**
   * Get statement.
   * @return Statement Statement
   */
  private Statement getStmt(){
    Statement stmt = null;
    try{
      if(stmt == null) {
        stmt = getCon().createStatement();
        stmt.setQueryTimeout(frameguard.getConfig().getInt("setting-query-timeout"));
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
    return stmt;
  }
  
  /**
   * Close connection.
   * @param Connection Connection
   */
  public void closeCon(){
    try{
      if(con != null){
        con.close();
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * Close connection.
   * @param Connection Connection
   */
  private static void closeCon(Connection con){
    try{
      if(con != null){
        con.close();
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * Close result set.
   * @param ResultSet Result set
   */
  private static void closeRs(ResultSet rs) {
    try{
      if(rs != null){
        rs.close();
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * Close statement.
   * @param Statement Statement
   */
  private static void closeStmt(Statement stmt) {
    try{
      if(stmt != null){
        stmt.close();
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * Close prepared statement.
   * @param PreparedStatement PreparedStatement
   */
  private static void closePrepStmt(PreparedStatement prepStmt){
    try{
      if(prepStmt != null){
        prepStmt.close();
      }
    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    }
  }
  
  /**
   * Initialize
   */
  public void initialize() {
    ResultSet rs = null;
    Statement stmt = null;
    try{

      stmt = getStmt();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS user ("
        + " id INTEGER PRIMARY KEY AUTOINCREMENT"
        + ",uuid STRING NOT NULL"
        + ",player_name STRING NOT NULL"
        + ",created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS uuid_index ON user (uuid);");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS player_name_index ON user (player_name);");

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS world ("
        + " id INTEGER PRIMARY KEY AUTOINCREMENT"
        + ",world_name STRING NOT NULL"
        + ",created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS world_name_index ON world (world_name);");

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS lockdata ("
        + " id INTEGER PRIMARY KEY AUTOINCREMENT"
        + ",user_id INTEGER NOT NULL"
        + ",world_id INTEGER NOT NULL"
        + ",x INTEGER NOT NULL"
        + ",y INTEGER NOT NULL"
        + ",z INTEGER NOT NULL"
        + ",block_face STRING NOT NULL"
        + ",attached_x INTEGER NOT NULL"
        + ",attached_y INTEGER NOT NULL"
        + ",attached_z INTEGER NOT NULL"
        + ",attached_material STRING NOT NULL"
        + ",created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ",unique(world_id, x, y, z)"
        + ");"
      );
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS user_id_world_id_x_y_z_index ON lockdata (user_id, world_id, x, y, z);");
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS created_at_index ON lockdata (created_at);");
      closeStmt(stmt);

    } catch (Exception e){
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closeStmt(stmt);
    }
  }
  
  /**
   * Get user id by player.
   * @param Player Player
   * @return Integer User id.
   */
  private Integer getUserId(Player player){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer userId = null;
    try {
      String uuid = player.getUniqueId().toString();
      prepStmt = getCon().prepareStatement("SELECT id FROM user WHERE uuid=?");
      prepStmt.setString(1, uuid);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        userId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      if(userId != null){
        return userId;
      }
      
      prepStmt = getCon().prepareStatement("INSERT INTO user(uuid, player_name) VALUES (?,?)");
      prepStmt.setString(1, uuid);
      prepStmt.setString(2, player.getName());
      prepStmt.addBatch();
      prepStmt.executeBatch();
      rs = prepStmt.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return userId;
  }
  
  /**
   * Get locked user id by entity.
   * @param Entity entity
   * @return Integer User id.
   */
  private Integer getLockedUserId(Entity entity){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer lockedUserId = null;
    try {
      Integer worldId = getWorldId(entity);
      if(worldId != null) {
        Location loc = entity.getLocation();
        prepStmt = getCon().prepareStatement("SELECT user_id FROM lockdata WHERE world_id=? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          lockedUserId = rs.getInt(1);
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
      }
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return lockedUserId;
  }
  
  /**
   * Get world id by entity.
   * @param Entity entity
   * @return Integer User id.
   */
  public Integer getWorldId(Entity entity){
    Location loc = entity.getLocation();
    return getWorldId(loc);
  }
  
  /**
   * Get world id by location.
   * @param Location Location
   * @return Integer User id.
   */
  private Integer getWorldId(Location loc) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer worldId = null;
    try {
      String world = loc.getWorld().getName();
      prepStmt = getCon().prepareStatement("SELECT id FROM world WHERE world_name=?");
      prepStmt.setString(1, world);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        worldId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      if(worldId != null){
        return worldId;
      }
      
      prepStmt = getCon().prepareStatement("INSERT INTO world(world_name) VALUES (?)");
      prepStmt.setString(1, world);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      rs = prepStmt.getGeneratedKeys();
      if (rs.next()) {
        worldId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return worldId;
  }
  
  /**
   * Get player name by user id.
   * @param Integer User id
   * @return String Player name.
   */
  private String getPlayerName(Integer userId){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String playerName = "";
    try {
      if(userId != null){
        prepStmt = getCon().prepareStatement("SELECT player_name FROM user WHERE id=?");
        prepStmt.setInt(1, userId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
          playerName = rs.getString(1);
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
      }
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return playerName;
  }
  
  /**
   * Finished process punch.
   * @param Player Player
   * @param Entity Entity
   */
  public void finishPunch(Player player, Entity entity){
    if( player.hasMetadata(FrameGuardCommand.META_LOCK)){
      FrameGuardUtility.removeAllPunch(player, frameguard);
      createLockData(player, entity);
    }else if( player.hasMetadata(FrameGuardCommand.META_UNLOCK)){
      FrameGuardUtility.removeAllPunch(player, frameguard);
      removeLockData(player, entity);
    }else if( player.hasMetadata(FrameGuardCommand.META_INFO)){
      FrameGuardUtility.removeAllPunch(player, frameguard);
      informationLockData(player, entity);
    }
  }
  
  /**
   * Create lock data.
   * @param Player Player
   * @param Entity Entity
   */
  public void createLockData(Player player, Entity entity){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      
      Hanging hanging = (Hanging) entity;
      if(hanging.getLocation().getBlock().isLiquid()){
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-can-not-lock-in-liquids"));
        return;
      }
      
      Block attachedBlock = FrameGuardUtility.getAttachedBlockByHanging(hanging);
      if(attachedBlock.isEmpty() || attachedBlock.isLiquid()){
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-can-not-lock-hanging-place-is-liquid-or-air"));
        return;
      }
      
      Integer userId = getUserId(player);
      Integer worldId = getWorldId(entity);
      Location loc = hanging.getLocation();
      Integer x = loc.getBlockX();
      Integer y = loc.getBlockY();
      Integer z = loc.getBlockZ();
      String blockFace = hanging.getFacing().name();
      Location attachedLoc = attachedBlock.getLocation();
      Integer attachedX = attachedLoc.getBlockX();
      Integer attachedY = attachedLoc.getBlockY();
      Integer attachedZ = attachedLoc.getBlockZ();
      String attachedMaterial = attachedBlock.getType().name();
      
      prepStmt = getCon().prepareStatement("SELECT id FROM lockdata WHERE world_id=? AND x=? AND y=? AND z=?");
      prepStmt.setInt(1, worldId);
      prepStmt.setInt(2, x);
      prepStmt.setInt(3, y);
      prepStmt.setInt(4, z);
      rs = prepStmt.executeQuery();
      Integer lockdataId = null;
      while(rs.next()){
        lockdataId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
      if(lockdataId == null){
        prepStmt = getCon().prepareStatement("INSERT INTO lockdata(user_id,world_id,x,y,z,block_face,attached_x,attached_y,attached_z,attached_material) VALUES (?,?,?,?,?,?,?,?,?,?)");
        prepStmt.setInt(1, userId);
        prepStmt.setInt(2, worldId);
        prepStmt.setInt(3, x);
        prepStmt.setInt(4, y);
        prepStmt.setInt(5, z);
        prepStmt.setString(6, blockFace);
        prepStmt.setInt(7, attachedX);
        prepStmt.setInt(8, attachedY);
        prepStmt.setInt(9, attachedZ);
        prepStmt.setString(10, attachedMaterial);
        prepStmt.addBatch();
        prepStmt.executeBatch();
        closePrepStmt(prepStmt);
        
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-locked"));
      }else{
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-already-locked"));
      }
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * Clean up world mater.
   * @param Integer worldId
   */
  public void cleanUpWorldMaster(Integer worldId) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      // Clean up world table
      prepStmt = getCon().prepareStatement("SELECT COUNT(id) FROM lockdata WHERE world_id=?");
      prepStmt.setInt(1, worldId);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        if(rs.getInt(1) < 1){
          prepStmt = getCon().prepareStatement("DELETE FROM world WHERE id=?");
          prepStmt.setInt(1, worldId);
          prepStmt.addBatch();
          prepStmt.executeBatch();
          closeRs(rs);
          closePrepStmt(prepStmt);
          break;
        }
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * Clean up user mater.
   * @param Integer userId
   */
  public void cleanUpUserMaster(Integer userId) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      // Clean up user table
      prepStmt = getCon().prepareStatement("SELECT COUNT(id) FROM lockdata WHERE user_id=?");
      prepStmt.setInt(1, userId);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        if(rs.getInt(1) < 1){
          prepStmt = getCon().prepareStatement("DELETE FROM user WHERE id=?");
          prepStmt.setInt(1, userId);
          prepStmt.addBatch();
          prepStmt.executeBatch();
          closeRs(rs);
          closePrepStmt(prepStmt);
        }
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * Remove lock data.
   * @param Player Player
   * @param Entity Entity
   */
  public void removeLockData(Player player, Entity entity){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      Integer worldId = getWorldId(entity);
      if(player.isOp() && worldId != null){
        prepStmt = getCon().prepareStatement("SELECT id, user_id FROM lockdata WHERE world_id=? AND x=? AND y=? AND z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, entity.getLocation().getBlockX());
        prepStmt.setInt(3, entity.getLocation().getBlockY());
        prepStmt.setInt(4, entity.getLocation().getBlockZ());
        rs = prepStmt.executeQuery();
        Integer lockDataId = null;
        Integer ownerUserId = null;
        while(rs.next()){
          lockDataId = rs.getInt(1);
          ownerUserId = rs.getInt(2);
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        
        prepStmt = getCon().prepareStatement("DELETE FROM lockdata WHERE id=?");
        prepStmt.setInt(1, lockDataId);
        prepStmt.addBatch();
        prepStmt.executeBatch();
        closePrepStmt(prepStmt);
        cleanUpUserMaster(ownerUserId);
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-unlocked"));
      } else {
        Integer userId = getUserId(player);
        if( userId != null && worldId != null && isLockedOwner(entity.getLocation(), player)) {
          prepStmt = getCon().prepareStatement("DELETE FROM lockdata WHERE user_id=? AND world_id=? AND x=? AND y=? AND z=?");
          prepStmt.setInt(1, userId);
          prepStmt.setInt(2, worldId);
          prepStmt.setInt(3, entity.getLocation().getBlockX());
          prepStmt.setInt(4, entity.getLocation().getBlockY());
          prepStmt.setInt(5, entity.getLocation().getBlockZ());
          prepStmt.addBatch();
          prepStmt.executeBatch();
          closePrepStmt(prepStmt);
          cleanUpUserMaster(userId);
          FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-unlocked"));
        }
      }
      
      cleanUpWorldMaster(worldId);
      
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * Purge lock data.
   * @param String days
   */
  public void purgeData(String days){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
        String strDate = "-__DAYS__ days";
        strDate.replace("__DAYS__", days);
        
        // Clean up
        prepStmt = getCon().prepareStatement("SELECT id, world_id, user_id FROM lockdata WHERE created_at < datetime('now', ?)");
        prepStmt.setString(1, strDate);
        rs = prepStmt.executeQuery();
        Set<Integer> delLockIds = new HashSet<Integer>();
        Set<Integer> delWorldIds = new HashSet<Integer>();
        Set<Integer> delUserIds = new HashSet<Integer>();
        while(rs.next()){
          delLockIds.add(rs.getInt(1));
          delWorldIds.add(rs.getInt(2));
          delUserIds.add(rs.getInt(3));
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        
        for (Integer id : delLockIds) {
          prepStmt = getCon().prepareStatement("DELETE FROM lockdata WHERE id = ?");
          prepStmt.setInt(1, id);
          prepStmt.addBatch();
          prepStmt.executeBatch();
          closePrepStmt(prepStmt);
        }
        
        for (Integer worldId : delWorldIds) {
          cleanUpWorldMaster(worldId);
        }
        
        for (Integer userId : delUserIds) {
          cleanUpUserMaster(userId);
        }
        
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * View lock data.
   * @param Player Player
   * @param Entity Entity
   */
  public void informationLockData(Player player, Entity entity){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      Integer lockedUserId = getLockedUserId(entity);
      if(lockedUserId != null){
        String playerName = getPlayerName(lockedUserId);
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-lock-by-player").replace("__PLAYERNAME__", playerName));
      } else {
        FrameGuardUtility.sendMessage(player, frameguard.getConfig().getString("message-no-lock-information"));
      }
    } catch (Exception e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
  }
  
  /**
   * Check lock data.
   * @param Location Location
   * @return boolean true:locked false:no lock
   */
  public boolean isLocked(Location loc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Boolean ret = false;
    try {
      Integer worldId = getWorldId(loc);
      
      if(worldId != null) {
        prepStmt = getCon().prepareStatement("SELECT id FROM lockdata WHERE world_id=? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = true;
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        return ret;
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Check lock data of attached by location.
   * @param Player Player
   * @param Entity Entity
   * @return boolean true:attached by lock false:no attached
   */
  public boolean isAttached(Location loc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Boolean ret = false;
    try {
      Integer worldId = getWorldId(loc);
      
      if(worldId != null) {
        prepStmt = getCon().prepareStatement("SELECT id FROM lockdata WHERE world_id=? and attached_x=? and attached_y=? and attached_z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = true;
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        return ret;
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Check lock data by location and player.
   * @param Location Location
   * @param Player Player
   * @return boolean true:locked false:no locked or no owner
   */
  public boolean isLockedOwner(Location loc, Player owner){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Boolean ret = false;
    try {
      Integer userId = getUserId(owner);
      Integer worldId = getWorldId(loc);
      
      if(worldId != null && userId != null) {
        prepStmt = getCon().prepareStatement("SELECT id FROM lockdata WHERE world_id=? and user_id =? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, userId);
        prepStmt.setInt(3, loc.getBlockX());
        prepStmt.setInt(4, loc.getBlockY());
        prepStmt.setInt(5, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = true;
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Get attached material.
   * @param Location Location
   * @return Material Attached material.
   */
  public Material getAttachedMaterial(Location loc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Material ret = null;
    try {
      Integer worldId = getWorldId(loc);
      
      if(worldId != null) {
        prepStmt = getCon().prepareStatement("SELECT attached_material FROM lockdata WHERE world_id=? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = Material.getMaterial(rs.getString(1));
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        
        return ret;
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Get attached location.
   * @param Location Location
   * @return Location Attached location.
   */
  public Location getAttachedLocation(Location loc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Location ret = null;
    try {
      Integer worldId = getWorldId(loc);
      
      if(worldId != null) {
        prepStmt = getCon().prepareStatement("SELECT attached_x,attached_y,attached_z FROM lockdata WHERE world_id=? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = new Location(loc.getWorld(),rs.getInt(1),rs.getInt(2), rs.getInt(3));
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        
        return ret;
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }

  /**
   * Get blockface.
   * @param Location Location
   * @return String blockface.
   */
  public String getBlockFace(Location loc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String ret = null;
    try {
      Integer worldId = getWorldId(loc);
      
      if(worldId != null) {
        prepStmt = getCon().prepareStatement("SELECT block_face FROM lockdata WHERE world_id=? and x=? and y=? and z=?");
        prepStmt.setInt(1, worldId);
        prepStmt.setInt(2, loc.getBlockX());
        prepStmt.setInt(3, loc.getBlockY());
        prepStmt.setInt(4, loc.getBlockZ());
        rs = prepStmt.executeQuery();
        while(rs.next()){
          ret = rs.getString(1);
        }
        closeRs(rs);
        closePrepStmt(prepStmt);
        
        return ret;
      }
    } catch (SQLException e) {
      FrameGuardUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }

}