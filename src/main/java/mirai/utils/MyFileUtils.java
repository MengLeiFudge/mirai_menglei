package mirai.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import mirai.core.MsgEvent;
import mirai.core.UnexpectedStateException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static mirai.utils.LogUtils.logError;
import static mirai.utils.LogUtils.logInfo;
import static mirai.utils.LogUtils.logWarn;
import static mirai.utils.Settings.DEBUG_MODE;

public class MyFileUtils {
    private MyFileUtils() {
    }

    /**
     * 指示各个基础目录.
     */
    public enum Dir {
        // 应用数据
        DATA("run", "data"),
        // 系统设置
        SETTINGS("run", "settings"),
        // 数据库
        DATABASE("run", "database");

        private final File dir;

        Dir(String... names) {
            dir = FileUtils.getFile(names);
        }

        File getDir() {
            return dir;
        }
    }

    //region 读写锁

    /**
     * 读写锁，只会增多不会减少.
     *
     * @see #getLock(File)
     * @see #getString(File, Object)
     * @see #setKeyValue(File, Object, Object)
     */
    public static Map<File, ReentrantReadWriteLock> fileLockMap = new ConcurrentHashMap<>(16);

    /**
     * 获取指定文件对应的锁.
     * <p>
     * 如果 {@link #fileLockMap} 不含所需的锁，则新增锁并返回；否则直接返回对应锁。
     *
     * @param file 要获取锁的文件
     * @return 获取成功返回锁，否则返回 null
     */
    private static synchronized ReentrantReadWriteLock getLock(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            logError(e);
            return null;
        }
        ReentrantReadWriteLock lock;
        try {
            if (fileLockMap.containsKey(file)) {
                lock = fileLockMap.get(file);
            } else {
                lock = new ReentrantReadWriteLock();
                fileLockMap.put(file, lock);
            }
            return lock;
        } catch (Exception e) {
            logError(e);
            return null;
        }
    }

    public static void lock(File... files) {
        if (files == null || files.length == 0) {
            return;
        }
        List<ReentrantReadWriteLock> locks = new ArrayList<>();
        for (File file : files) {
            ReentrantReadWriteLock lock = getLock(file);
            if (lock == null) {
                return;
            }
            locks.add(lock);
        }
        for (int i = 0; i < locks.size(); i++) {
            if (DEBUG_MODE) {
                logInfo("开始锁定 " + files[i].getPath());
            }
            try {
                // 3s未能锁定则认为失败
                boolean lockSuccess = locks.get(i).writeLock().tryLock(3, TimeUnit.SECONDS);
                if (lockSuccess) {
                    if (DEBUG_MODE) {
                        logInfo("锁定成功 " + files[i].getPath());
                    }
                } else {
                    logError(new UnexpectedStateException("锁定失败 " + files[i].getPath()));
                }
            } catch (InterruptedException e) {
                logError("锁定错误 " + files[i].getPath(), e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 解锁指定的文件.
     * <p>
     * 如果目标文件为null，则忽略
     *
     * @param files 要解锁的文件
     */
    public static synchronized void unlock(File... files) {
        if (files == null || files.length == 0) {
            return;
        }
        List<ReentrantReadWriteLock> locks = new ArrayList<>();
        for (File file : files) {
            ReentrantReadWriteLock lock = getLock(file);
            if (lock == null) {
                return;
            }
            locks.add(lock);
        }
        for (int i = 0; i < locks.size(); i++) {
            if (DEBUG_MODE) {
                logInfo("开始解锁 " + files[i].getPath());
            }
            if (locks.get(i).writeLock().isHeldByCurrentThread()) {
                locks.get(i).writeLock().unlock();
                if (DEBUG_MODE) {
                    logInfo("解锁完成 " + files[i].getPath());
                }
            } else {
                logError(new UnexpectedStateException("解锁错误 " + files[i].getPath()));
            }
        }
    }

    //endregion

    //region 文件读写（以键值对方式）

    /**
     * 文件不存在，或文件中不存在键时，将返回默认值.
     */
    public static final String DEF_STRING = "-2147483648";
    public static final int DEF_INT = Integer.parseInt(DEF_STRING);
    public static final long DEF_LONG = Long.parseLong(DEF_STRING);
    public static final double DEF_DOUBLE = Double.parseDouble(DEF_STRING);
    /**
     * 无法从文件中读取键值对，或值的类型不匹配时，将返回错误值.
     */
    public static final String ERR_STRING = "-2147483647";
    public static final int ERR_INT = Integer.parseInt(ERR_STRING);
    public static final long ERR_LONG = Long.parseLong(ERR_STRING);
    public static final double ERR_DOUBLE = Double.parseDouble(ERR_STRING);

    /**
     * 以 UTF_8 编码读取一个字符串.
     * <ul>
     * <li>文件存在，且获取到 {@code key} 对应的值，返回该值
     * <li>文件不存在，或文件存在但无 {@code key}，返回 {@link #DEF_STRING}
     * <li>{@code key} 类型出错返回 {@link #ERR_STRING}
     * </ul>
     *
     * @param file 路径
     * @param key  关键字
     * @return 获取到的字符串
     */
    public static String getString(File file, Object key) {
        if (!(key instanceof String) && !(key instanceof Integer) && !(key instanceof Long)) {
            logError(new IllegalArgumentException("key 必须继承于 String/Integer/Long\npath：" + file));
            return ERR_STRING;
        }
        if (!file.exists()) {
            return DEF_STRING;
        }
        if (file.isDirectory()) {
            logError(new Exception("已存在文件夹\npath：" + file + "\nkey：" + key));
            return ERR_STRING;
        }
        lock(file);
        Properties properties = new Properties();
        try (InputStreamReader input = new InputStreamReader
                (new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(input);
            String ret = properties.getProperty(key + "");
            if (ret == null) {
                ret = DEF_STRING;
            }
            return ret;
        } catch (IOException e) {
            logError("path：" + file + "\nkey：" + key, e);
            return ERR_STRING;
        } finally {
            unlock(file);
        }
    }

    public static int getInt(File file, Object key) {
        try {
            return Integer.parseInt(getString(file, key));
        } catch (NumberFormatException e) {
            logError(e);
            return ERR_INT;
        }
    }

    public static long getLong(File file, Object key) {
        try {
            return Long.parseLong(getString(file, key));
        } catch (NumberFormatException e) {
            logError(e);
            return ERR_LONG;
        }
    }

    public static double getDouble(File file, Object key) {
        try {
            return Double.parseDouble(getString(file, key));
        } catch (NumberFormatException e) {
            logError(e);
            return ERR_DOUBLE;
        }
    }

    public static boolean getBoolean(File file, Object key) {
        try {
            return Boolean.parseBoolean(getString(file, key));
        } catch (NumberFormatException e) {
            logError(e);
            return false;
        }
    }

    /**
     * 以 UTF_8 编码写入一个字符串.
     *
     * @param file 路径
     * @param key  关键字
     * @param val  要写入的值
     */
    public static void setKeyValue(File file, Object key, Object val) {
        if (!(key instanceof String) && !(key instanceof Integer)
                && !(key instanceof Long) && !(key instanceof Boolean)) {
            logError("写入键值对错误", new Exception(
                    "关键字只能是String、Integer、Long或Boolean\n" + file));
            return;
        }
        if (!(val instanceof String) && !(val instanceof Integer)
                && !(val instanceof Long) && !(val instanceof Double) && !(val instanceof Boolean)) {
            logError("写入键值对错误", new Exception(
                    "值只能是String、Integer、Long、Double或Boolean\n" + file));
            return;
        }
        if (!createFileIfNotExists(file)) {
            logError(new UnexpectedStateException("文件创建失败\n" +
                    "path：" + file + "\nkey：" + key + "\nval：" + val));
            return;
        }
        if (file.isDirectory()) {
            logError(new Exception(
                    "已存在文件夹\npath：" + file + "\nkey：" + key + "\nval：" + val));
            return;
        }
        lock(file);
        Properties properties = new Properties();
        try (InputStreamReader input = new InputStreamReader
                (new FileInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(input);
        } catch (IOException e) {
            logError("path：" + file + "\nkey：" + key + "\nval：" + val, e);
            unlock(file);
            return;
        }
        properties.setProperty(key + "", val + "");
        try (OutputStreamWriter output = new OutputStreamWriter
                (new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // 注意，new的时候，由于没有append，所以文件已经被清空
            // 如果跟上面的输入流一起建立，输入流就只能读空文件
            properties.store(output, null);
        } catch (IOException e) {
            logError("path：" + file + "\nkey：" + key + "\nval：" + val, e);
        } finally {
            unlock(file);
        }
    }

    //endregion

    //region 序列化与反序列化

    private static final Map<MsgEvent, List<File>> LOCK_FILES_MAP = new ConcurrentHashMap<>(16);

    /**
     * 将对象序列化为文件.
     * 传入的对象不应为空，请在序列化之前进行检查。
     *
     * @param obj  被序列化的对象
     * @param file 序列化文件的存储位置
     */
    public static void serialize(Object obj, File file) {
        if (obj == null) {
            return;
        }
        lock(file);
        try {
            if (!createFileIfNotExists(file)) {
                logError(new UnexpectedStateException("无法创建文件\n" +
                        "path：" + file));
                return;
            }
            if (file.isDirectory() || !file.canWrite()) {
                logError(new UnexpectedStateException("已存在文件夹或文件无法写入\n" +
                        "path：" + file));
                return;
            }
            FileUtils.writeStringToFile(file, JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logError("path：" + file, e);
        } finally {
            unlock(file);
        }
    }

    /**
     * 将文件反序列化为对象.
     *
     * @param file              被反序列化的文件
     * @param destClazz         目标类的class
     * @param unlockAtMethodEnd 反序列化完成时是否释放锁。
     *                          如果仅读取而不写入，则传入 true；如果有写入的可能，则传入 false。
     * @param <T>               目标类
     * @return 反序列化得到的指定类的实例对象
     */
    private static <T> T deserialize(File file, Class<T> destClazz, boolean unlockAtMethodEnd, MsgEvent msgEvent) {
        if (!file.exists()) {
            return null;
        }
        lock(file);
        try {
            return JSON.parseObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8), destClazz);
        } catch (IOException e) {
            logError("path：" + file, e);
            return null;
        } finally {
            if (unlockAtMethodEnd) {
                unlock(file);
            } else {
                List<File> fileList;
                if (!LOCK_FILES_MAP.containsKey(msgEvent)) {
                    fileList = new ArrayList<>();
                    LOCK_FILES_MAP.put(msgEvent, fileList);
                } else {
                    fileList = LOCK_FILES_MAP.get(msgEvent);
                }
                fileList.add(file);
            }
        }
    }

    /**
     * 将文件反序列化为对象，并且在反序列化完成时保留锁，等待最终处理完成后再解锁.
     *
     * @param file      被反序列化的文件
     * @param destClazz 目标类的class
     * @param msgEvent  用于最终处理完成后，在释放锁的消息事件中找到锁
     * @param <T>       目标类
     * @return 反序列化得到的指定类的实例对象
     */
    public static <T> T deserializeThenNotUnlock(File file, Class<T> destClazz, MsgEvent msgEvent) {
        return deserialize(file, destClazz, false, msgEvent);
    }

    /**
     * 将文件反序列化为对象，并且在反序列化完成时解除锁.
     *
     * @param file      被反序列化的文件
     * @param destClazz 目标类的class
     * @param <T>       目标类
     * @return 反序列化得到的指定类的实例对象
     */
    public static <T> T deserializeThenUnlock(File file, Class<T> destClazz) {
        return deserialize(file, destClazz, true, null);
    }

    public static void unlockFiles(MsgEvent msgEvent) {
        if (LOCK_FILES_MAP.containsKey(msgEvent)) {
            for (File f : LOCK_FILES_MAP.get(msgEvent)) {
                unlock(f);
            }
            LOCK_FILES_MAP.remove(msgEvent);
        }
    }

    //endregion


    public static File getFile(Dir dir, String... names) {
        return getFile(dir.getDir(), names);
    }

    public static File getFile(File dir, String... names) {
        try {
            return FileUtils.getFile(dir, names).getCanonicalFile();
        } catch (IOException e) {
            logError(e);
            return null;
        }
    }

    /**
     * 文件不存在时，创建该文件.
     * <p>
     * 文件存在时，直接返回 {@code true}；否则返回是否成功创建该文件。
     *
     * @param file 目标文件
     * @return 文件是否已存在
     */
    public static boolean createFileIfNotExists(File file) {
        if (file.isFile()) {
            return true;
        }
        return forceCreateNewFile(file);
    }

    /**
     * 强制创建新文件.
     * <p>
     * 如果文件已存在，则删除后创建新文件；否则直接创建新文件。
     * <p>
     * 仅当新文件创建成功时返回 {@code true}。
     *
     * @param file 目标文件
     * @return 文件是否已存在
     */
    public static boolean forceCreateNewFile(File file) {
        if (file.isDirectory()) {
            logError(new UnexpectedStateException("已存在文件夹 " + file.getPath() + "，无法创建文件"));
            return false;
        }
        deleteIfExists(file);
        try {
            FileUtils.forceMkdirParent(file);
            if (file.createNewFile()) {
                logInfo("文件 " + file.getCanonicalPath() + " 创建成功");
                return true;
            } else {
                logError(new IOException("文件 " + file.getCanonicalPath() + " 创建失败"));
                return false;
            }
        } catch (NullPointerException | IOException e) {
            logError("文件 " + file.getAbsolutePath() + " 创建失败", e);
            return false;
        }
    }

    public static boolean mkdir(File dir) {
        if (dir.isDirectory()) {
            return true;
        }
        try {
            FileUtils.forceMkdir(dir);
            logInfo("文件夹 " + dir.getCanonicalPath() + " 创建成功");
            return true;
        } catch (NullPointerException | IOException e) {
            logError("文件夹 " + dir.getAbsolutePath() + " 创建失败", e);
            return false;
        }
    }

    /**
     * 移动原始文件至目标文件（目标文件不能存在）.
     *
     * @param srcFile  原始文件
     * @param destFile 目标文件
     * @return 移动结果
     */
    public static boolean moveFile(File srcFile, File destFile) {
        try {
            srcFile = srcFile.getCanonicalFile();
            destFile = destFile.getCanonicalFile();
            FileUtils.moveFile(srcFile, destFile);
            return true;
        } catch (IOException e) {
            logError(e);
            return false;
        }
    }

    public static boolean moveFile(String srcFilePath, String destFilePath) {
        return moveFile(new File(srcFilePath), new File(destFilePath));
    }

    /**
     * 移动原始文件/文件夹至目标文件夹或目标文件夹内.
     * <p>
     * {@code moveInside} 为 {@code true} 时，表示移动原始文件/文件夹至目标文件夹内部；
     * 否则表示移动原始文件夹至目标文件夹（目标文件夹不能存在）。
     *
     * @param src        原始文件/文件夹
     * @param destDir    目标文件夹
     * @param moveInside 是否移动至文件夹内部
     * @return 移动结果
     */
    public static boolean moveToDir(File src, File destDir, boolean moveInside) {
        try {
            src = src.getCanonicalFile();
            destDir = destDir.getCanonicalFile();
            if (moveInside) {
                FileUtils.moveToDirectory(src, destDir, true);
            } else {
                FileUtils.moveDirectory(src, destDir);
            }
            return true;
        } catch (IOException e) {
            logError(e);
            return false;
        }
    }

    public static boolean moveToDir(String src, String destDir, boolean moveInside) {
        return moveToDir(new File(src), new File(destDir), moveInside);
    }

    public static boolean copy() {

        //FileUtils.copyDirectory(srcdir, destdir, filter);
        return true;
    }

    /**
     * 重命名文件或文件夹.
     *
     * @param fileOrDir 原文件/文件夹
     * @param newName   新文件名/文件夹名，后缀可加可不加
     * @return 重命名成功返回true，否则返回false
     */
    public static boolean rename(File fileOrDir, String newName) {
        File parent = fileOrDir.getParentFile();
        if (!fileOrDir.isDirectory()) {
            String sourceName = fileOrDir.getName();
            if (sourceName.contains(".") && !newName.contains(".")) {
                newName = newName + "." + sourceName.substring(sourceName.lastIndexOf('.'));
            }
        }
        return fileOrDir.renameTo(new File(parent, newName));
    }

    public static boolean rename(String filePath, String newName) {
        return rename(new File(filePath), newName);
    }

    /**
     * 删除文件或文件夹.
     *
     * @param fileOrDir 想删除的文件/文件夹
     * @return 文件不存在，或文件已被删除时返回true，否则返回false
     */
    public static boolean deleteIfExists(File fileOrDir) {
        if (!fileOrDir.exists()) {
            return true;
        }
        try {
            if (fileOrDir.isDirectory()) {
                FileUtils.deleteDirectory(fileOrDir.getCanonicalFile());
            } else {
                FileUtils.forceDelete(fileOrDir.getCanonicalFile());
            }
            return true;
        } catch (FileNotFoundException e) {
            logWarn(e);
            return true;
        } catch (IOException e) {
            logError(e);
            return false;
        }
    }

    public static boolean deleteIfExists(String filePath) {
        return deleteIfExists(new File(filePath));
    }

}
