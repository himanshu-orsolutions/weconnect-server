package com.connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The ChatServer. It holds implementation to connect friends.
 */
public class ChatServer {

	/**
	 * The count of online friends
	 */
	private int onlineFriends = 0;

	/**
	 * The server socket
	 */
	private ServerSocket serverSocket;

	/**
	 * The message reader for the first friend
	 */
	private BufferedReader firstMessageReader;

	/**
	 * The message reader for the second friend
	 */
	private BufferedReader secondMessageReader;

	/**
	 * The message sender for the first friend
	 */
	private BufferedWriter firstMessageWriter;

	/**
	 * The message sender for the second friend
	 */
	private BufferedWriter secondMessageWriter;

	/**
	 * The unsynchronized chat task executor
	 */
	private Executor executor = Executors.newCachedThreadPool();

	/**
	 * Opens the chat channel
	 */
	private void openChatChannel() {

		this.executor.execute(() -> { // Handle messages from Friend I

			try {
				while (onlineFriends == 2) {
					String message = firstMessageReader.readLine();
					System.out.println("Friend I: " + message);

					if (message != null) {
						secondMessageWriter.write(message + '\n');
						secondMessageWriter.flush();

						if (message.equals("bye")) {
							onlineFriends--;
							break;
						}
					} else {
						onlineFriends--;
						break;
					}
				}
			} catch (IOException ioException) {
				System.out.println("Closing connection with the Friend I.");
			} finally {
				try {
					firstMessageReader.close();
					firstMessageWriter.flush();
					firstMessageWriter.close();
				} catch (IOException ioException) {
					System.out.println("Error closing the streams for Friend I.");
				}
			}
			waitForTwoFriends();
		});

		this.executor.execute(() -> { // Handle message from Friend II

			try {
				while (onlineFriends == 2) {
					String message = secondMessageReader.readLine();
					System.out.println("Friend II: " + message);

					if (message != null) {
						firstMessageWriter.write(message + '\n');
						firstMessageWriter.flush();

						if (message.equals("bye")) {
							onlineFriends--;
							break;
						}
					} else {
						onlineFriends--;
						break;
					}
				}
			} catch (IOException ioException) {
				System.out.println("Closing connection with the Friend II.");
			} finally {
				try {
					secondMessageReader.close();
					secondMessageWriter.flush();
					secondMessageWriter.close();
				} catch (IOException ioException) {
					System.out.println("Error closing the streams for Friend II.");
				}
			}
			waitForTwoFriends();
		});
	}

	/**
	 * Waits for two friends to join the channel
	 */
	private void waitForTwoFriends() {

		System.out.println("Waiting for two friends to join the channel!!");
		while (onlineFriends < 2) {
			try {
				Socket socket = serverSocket.accept();

				if (onlineFriends == 0) {
					System.out.println("Friend I joined :)");
					firstMessageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					firstMessageWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				} else {
					System.out.println("Friend II joined :)");
					secondMessageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					secondMessageWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				}
				onlineFriends++;
			} catch (IOException ioException) {
				System.out.println("Error accepting the connections!!");
			}
		}

		// Opens the chat channel
		System.out.println("Opening the chat channel!!");
		openChatChannel();
	}

	/**
	 * Instantiating the chat server
	 */
	public ChatServer() {

		try {
			serverSocket = new ServerSocket(9090);
			waitForTwoFriends();
		} catch (IOException ioException) {
			System.out.println("Error creating the server socket!!");
		}
	}

	/**
	 * Execution starts from here
	 * 
	 * @param args The command line arguments
	 */
	public static void main(String[] args) {

		new ChatServer();
	}
}
