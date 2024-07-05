 In this project, we developed a secure P2P messaging app that allows users to send messages
 between any two users with end-to-end encryption guarantees. The app will provide standard
 reliability guarantees, including message delivery and ordering. Additionally, we will implement a
 client interface for testing and visualization purposes, which will display the list of conversations of a
 user and the contents of a specified conversation. For the implementation of the messaging desktop
 app, we used the latest Java version, Java 21, and the software platform JavaFX. We have chosen
 to implement two extra functionalities: long-term storage of messages and group conversations. For
 long-term storage of messages, we replicate a file containing a copy of the messages to various cloud
 providers. This file is encrypted using an Advanced Encryption Standard (AES) key, ensuring the
 security and integrity of the data. The AES key is divided into shares, each of which is stored with
 a different cloud provider. The cloud providers used were Dropbox, Google Drive, and Github.
 Additionally, we streamlined the integration of cloud storage APIs using the build automation tool
 Maven. This approach guarantees the availability of the messages, as they can be recovered even if
 the user’s device or a single provider is compromised. For the group conversation functionality, we
 used a ciphertext-policy attribute-based encryption (CP-ABE) scheme. This scheme will provide
 secure access control, ensuring that only group members can view the messages.
