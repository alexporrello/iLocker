package org.olerpler.iLocker.encryption;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * A utility class that encrypts or decrypts a file.
 * @author www.codejava.net
 *
 */
public class EncryptionManager {
	public static final File ILOCKER_FOLDER   = new File(System.getenv("APPDATA") + "\\iLocker");

	public static final File DECRYPTED_FOLDER = new File(ILOCKER_FOLDER.getAbsolutePath() + "\\decrypted");
	public static final File DECRYPTED_TEST   = new File(DECRYPTED_FOLDER + "\\encrypted_file");

	public static final File ENCRYPTED_FOLDER = new File(ILOCKER_FOLDER.getAbsolutePath() + "\\encrypted");
	public static final File ENCRYPTED_TEST   = new File(ENCRYPTED_FOLDER + "\\encrypted_file");

	public static final File PREFERENCES      = new File(ILOCKER_FOLDER + "\\iLockerPrefs.xml");

	private String password;

	private HashMap<String, Long> files = new HashMap<String, Long>();

	public Preferences preferences = new Preferences();

	public void runProgram(String password) throws EncryptionException, IOException {
		this.password = makePasswordEighteenBytes(password);

		if(!ENCRYPTED_TEST.exists()) {
			ILOCKER_FOLDER.mkdir();
			DECRYPTED_FOLDER.mkdir();
			ENCRYPTED_FOLDER.mkdir();
			DECRYPTED_TEST.createNewFile();
			CryptoUtils.encrypt(this.password, DECRYPTED_TEST, ENCRYPTED_TEST);
		} else {
			CryptoUtils.decrypt(this.password, ENCRYPTED_TEST, DECRYPTED_TEST);
			preferences.read();
			if(preferences.decryptAllOnOpen) {
				for(File f : ENCRYPTED_FOLDER.listFiles()) {
					openFile(f, false);
				}
			}
		}
	}

	public void openFile(File file, boolean openInExplorer) {
		if(file.exists()) {
			try {
				File encrypted = new File(ENCRYPTED_FOLDER + "\\" + file.getName());
				File decrypted = new File(DECRYPTED_FOLDER + "\\" + file.getName());

				if(!decrypted.exists()) {
					CryptoUtils.decrypt(password, encrypted, decrypted);
					files.put(decrypted.getName(), decrypted.lastModified());
				}

				if(openInExplorer) {
					try {
						Desktop.getDesktop().open(decrypted);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (EncryptionException e) {
				e.printStackTrace();
			}			
		}
	}

	/**
	 * Manages the encryption/decryption on program close.
	 */
	public void exit() {
		File[] decryptedFiles = DECRYPTED_FOLDER.listFiles();

		for(File f : decryptedFiles) {
			try {
				File encrypted = new File(ENCRYPTED_FOLDER + "\\" + f.getName());
				File decrypted = new File(DECRYPTED_FOLDER + "\\" + f.getName());

				if(files.containsKey(decrypted.getName())) {
					if(!files.get(decrypted.getName()).equals(f.lastModified())) {
						System.out.println(decrypted.getName() + " was encrypted");
						CryptoUtils.encrypt(password, decrypted, encrypted);
					}
				} else {
					CryptoUtils.encrypt(password, decrypted, encrypted);
				}

				decrypted.delete();
			} catch (EncryptionException e) {
				e.printStackTrace();
			}			
		}

		preferences.write();
	}

	

	//	/**
	//	 * Reads the preferences from the application data folder 
	//	 * for the user's OS.
	//	 * @return true if the file could be read; else, false.
	//	 */
	//	private Boolean readPreferences() {
	//		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
	//			try {
	//				String url;
	//
	//				if(!PREFERENCES.exists()) {
	//					PREFERENCES.createNewFile();
	//				}
	//
	//				BufferedReader br = new BufferedReader(new FileReader(PREFERENCES));
	//
	//				while ((url = br.readLine()) != null) {
	//					if(!url.equals("")) {
	//						if(url.contains("<decryptAll>")) {
	//							url = url.replace("<decryptAll>", "");
	//							url = url.replace("</decryptAll>", "");
	//							decryptAll = Boolean.parseBoolean(url);
	//						}
	//					}
	//				}
	//
	//				br.close();
	//
	//				return true;
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		} else {
	//			decryptAll = false;
	//		}
	//
	//		return false;
	//	}

	//	/**
	//	 * Saves the user's preferences to the OS's app data folder.
	//	 * @return true if the prefs could be saved; else, false.
	//	 */
	//	private Boolean writePreferences() {
	//		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
	//			try {
	//				String content = "<decryptAll>" + decryptAll + "</decryptAll>\n";
	//				FileWriter fw = new FileWriter(PREFERENCES.getAbsoluteFile());
	//				BufferedWriter bw = new BufferedWriter(fw);
	//				bw.write(content);
	//				bw.close();
	//
	//				return true;
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		return false;
	//	}

	/**
	 * This is likely temporary (TODO). This method of encryption requires 
	 * a key that is 18 bytes long.
	 * @param password the password entered by the user
	 * @return a password that has been lengthened or shortened to 18 bytes
	 */
	private static String makePasswordEighteenBytes(String password) {
		if(password.length() < 16) {
			while(password.length() < 16) {
				password = password + "0";
			}
		} else {
			password = password.substring(0, 15);
		}

		return password;
	}

	/**
	 * A utility class that encrypts or decrypts a file.
	 * @author www.codejava.net
	 */
	private static class CryptoUtils {
		private static final String ALGORITHM = "AES";
		private static final String TRANSFORMATION = "AES";

		public static void encrypt(String key, File inputFile, File outputFile) 
				throws EncryptionException {
			doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
		}

		public static void decrypt(String key, File inputFile, File outputFile) 
				throws EncryptionException {
			doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
		}

		private static void doCrypto(int cipherMode, String key, File inputFile, 
				File outputFile) throws EncryptionException {
			try {
				Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
				Cipher cipher = Cipher.getInstance(TRANSFORMATION);
				cipher.init(cipherMode, secretKey);

				FileInputStream inputStream = new FileInputStream(inputFile);
				byte[] inputBytes = new byte[(int) inputFile.length()];
				inputStream.read(inputBytes);

				byte[] outputBytes = cipher.doFinal(inputBytes);

				FileOutputStream outputStream = new FileOutputStream(outputFile);
				outputStream.write(outputBytes);

				inputStream.close();
				outputStream.close();

			} catch (NoSuchPaddingException | NoSuchAlgorithmException
					| InvalidKeyException | BadPaddingException
					| IllegalBlockSizeException | IOException ex) {
				throw 
				new EncryptionException("Error encrypting/decrypting file", ex);
			}
		}
	}
}
