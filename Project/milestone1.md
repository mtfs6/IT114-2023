<table><tr><td> <em>Assignment: </em> It114 Milestone1</td></tr>
<tr><td> <em>Student: </em> Mohamed Saad (mts6)</td></tr>
<tr><td> <em>Generated: </em> 4/22/2023 1:11:58 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-006-S23/it114-milestone1/grade/mts6" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <ol><li>Create a new branch called Milestone1</li><li>At the root of your repository create a folder called Project</li><ol><li>You will be updating this folder with new code as you do milestones</li><li>You won't be creating separate folders for milestones; milestones are just branches</li></ol><li>Create a milestone1.md file inside the Project folder</li><li>Git add/commit/push it to Github</li><li>Create a pull request from Milestone1 to main (don't complete/merge it yet)</li><li>Copy in the latest Socket sample code from the most recent Socket Part example of the lessons</li><ol><li>Recommended Part 5 (clients should be having names at this point and not ids)</li><li><a href="https://github.com/MattToegel/IT114/tree/Module5/Module5">https://github.com/MattToegel/IT114/tree/Module5/Module5</a>&nbsp;<br></li></ol><li>Git add/commit the baseline</li><li>Ensure the sample is working and fill in the below deliverables</li><li>Get the markdown content or the file and paste it into the milestone1.md file or replace the file with the downloaded version</li><li>Git add/commit/push all changes</li><li>Complete the pull request merge from step 5</li><li>Locally checkout main</li><li>git pull origin main</li></ol></td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Startup </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot showing your server being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229355536-b25a7e34-a961-4e69-a321-f1baaeb4127f.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Server Running<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshot showing your client being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229359059-32b0eaaa-ff8a-4b56-bd9c-a4025ff2fe3a.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Client Started<br></p>
</td></tr>
<tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229359370-c2e0a2e1-0366-40cf-887e-58493640867a.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Client Connected<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the connection process</td></tr>
<tr><td> <em>Response:</em> <div>#1 A socket has been created at port 12345 for accepting new connections.<br>When socket is successfully created, a message has been displayed showing the message<br>"Our server is still running..."</div><div>#2 A socket has been created at the client<br>end at port 12345 for the output stream.&nbsp; When the output stream socket<br>is successfully created then "This is client side. Send message".</div><div>#3 When the server<br>is started at a port 12345, its waiting for the input string to<br>be readout in a While loop, when input is sent from the client<br>then server accept it and printout at command line<br></div><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Sending/Receiving </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229359059-32b0eaaa-ff8a-4b56-bd9c-a4025ff2fe3a.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Client Connected<br></p>
</td></tr>
<tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229359059-32b0eaaa-ff8a-4b56-bd9c-a4025ff2fe3a.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Client Sent Message<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the messages are sent, broadcasted, and received</td></tr>
<tr><td> <em>Response:</em> <div>#1 When a client is connected to a server through a specific port<br>the it could send message through outstream</div><div>#2 Server is listening at a specific<br>port, and when a client sent request for connection then it accept it<br>and connect to its configured port.</div><div>#3 When multiple client connected with server then<br>server broadcast message to all clients, as each client is connected to a<br>specific port so server broadcast message to this port which all clients can<br>receive.</div><div>#4 Each client connected to server at a specific port and could receive<br>message which is broadcast from server side.<br></div><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Disconnecting / Terminating </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229363927-7628dccf-74df-4b29-b9e0-51be0b7766b1.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Server Message when Client Disconnected<br></p>
</td></tr>
<tr><td><img width="768px" src="https://user-images.githubusercontent.com/106442036/229364031-62ba90f4-fcb2-4487-ac0d-9a3c1565a7de.jpg"/></td></tr>
<tr><td> <em>Caption:</em> <p>Client Exit<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the various disconnects/terminations are handled</td></tr>
<tr><td> <em>Response:</em> <div>#1 When a client types "exit" it will be disconnected</div><div>#2 When the server<br>disconnected then client program can keep sending messages on the open port and<br>hence not crashes</div><div>#3 When client disconnects then it simply closes its connection to<br>the port which will not effect server program<br></div><div><br></div><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707795-a9c94a71-7871-4572-bfae-ad636f8f8474.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add the pull request for this branch</td></tr>
<tr><td>Not provided</td></tr>
<tr><td> <em>Sub-Task 2: </em> Talk about any issues or learnings during this assignment</td></tr>
<tr><td> <em>Response:</em> <p>(missing)</p><br></td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-006-S23/it114-milestone1/grade/mts6" target="_blank">Grading</a></td></tr></table>