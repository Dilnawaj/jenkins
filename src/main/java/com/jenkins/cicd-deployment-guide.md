# CI/CD Pipeline Deployment Guide
### Deploy Spring Boot App on AWS EC2 with Jenkins + GitHub

---

## OVERVIEW — What This Guide Does

```
git push → GitHub Webhook → Jenkins Auto Build → Deploy to EC2 → App Live ✅
```

---

## PART 1 — AWS EC2 Setup

### Step 1.1 — Launch EC2 Instance
- AMI: Ubuntu 22.04 or 24.04 LTS
- Instance type: t2.micro (Free Tier)
- Storage: **20GB** (Free Tier allows up to 30GB)
- Key pair: Download your .pem file safely
- Security Group — open these ports:
  - Port 22   → SSH
  - Port 8080 → Jenkins
  - Port 8081 → Your Spring Boot App (change if your app uses different port)

### Step 1.2 — SSH Into Your Server
```bash
ssh -i "your-key.pem" ubuntu@YOUR_EC2_PUBLIC_IP
```

---

## PART 2 — Server Preparation

### Step 2.1 — Install Java 21 (Required for Jenkins 2.5xx+)
```bash
sudo apt update
sudo apt install openjdk-21-jdk -y

# Set Java 21 as default
sudo update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java

# Verify
java -version
# Should show: openjdk version "21..."
```

### Step 2.2 — Install Maven
```bash
sudo apt install maven -y

# Verify
mvn -version
```

### Step 2.3 — Add Swap Space (Important for small EC2 instances)
```bash
# Create 2GB swap file
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# Make permanent across reboots
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify
free -h
```

### Step 2.4 — Expand /tmp Size (Prevents Jenkins disk space warnings)
```bash
sudo mount -o remount,size=2G /tmp

# Verify
df -h /tmp
# Should show: tmpfs 2.0G
```

---

## PART 3 — Install Jenkins (WAR Method — Works on All Ubuntu Versions)

### Step 3.1 — Download Jenkins WAR
```bash
sudo wget -O /opt/jenkins.war \
  https://get.jenkins.io/war-stable/latest/jenkins.war
```

### Step 3.2 — Create Jenkins systemd Service
```bash
sudo tee /etc/systemd/system/jenkins.service > /dev/null <<EOF
[Unit]
Description=Jenkins Server
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/jenkins.war --httpPort=8080
Restart=on-failure
Environment="JENKINS_HOME=/home/ubuntu/.jenkins"

[Install]
WantedBy=multi-user.target
EOF
```

### Step 3.3 — Start Jenkins
```bash
sudo systemctl daemon-reload
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Check status
sudo systemctl status jenkins
```

### Step 3.4 — Get Initial Admin Password
```bash
# Wait ~30 seconds for Jenkins to initialize, then:
cat /home/ubuntu/.jenkins/secrets/initialAdminPassword
```

### Step 3.5 — Access Jenkins UI
- Open browser: `http://YOUR_EC2_PUBLIC_IP:8080`
- Paste the password from Step 3.4
- Click **"Install Suggested Plugins"** and wait
- Create your admin user

---

## PART 4 — Configure Jenkins

### Step 4.1 — Install Required Plugins
Go to: **Manage Jenkins → Plugins → Available**

Search and install:
- ✅ SSH Agent Plugin
- ✅ GitHub Integration Plugin

### Step 4.2 — Configure Maven in Jenkins
Go to: **Manage Jenkins → Tools → Maven installations → Add Maven**
- Name: `Maven`  ← must be exactly this
- ✅ Check "Install automatically"
- Version: `3.9.9`
- Click **Save**

### Step 4.3 — Fix Disk Space Monitor (Prevents node going offline)
```bash
# Stop Jenkins
sudo systemctl stop jenkins
sleep 3

# Disable disk space monitors in config
sed -i 's|<disabledAdministrativeMonitors>|<disabledAdministrativeMonitors>\n    <string>hudson.node_monitors.DiskSpaceMonitor</string>\n    <string>hudson.node_monitors.TemporarySpaceMonitor</string>|g' \
  /home/ubuntu/.jenkins/config.xml

# Start Jenkins again
sudo systemctl start jenkins
sleep 20
```

---

## PART 5 — Create systemd Service for Your Spring Boot App

```bash
# Create the service (run this ONCE per app)
sudo tee /etc/systemd/system/springboot.service > /dev/null <<EOF
[Unit]
Description=Spring Boot App
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/YOUR-APP-NAME.jar
Restart=always
StandardOutput=append:/home/ubuntu/app.log
StandardError=append:/home/ubuntu/app.log

[Install]
WantedBy=multi-user.target
EOF

# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable springboot
sudo systemctl start springboot

# Check status
sudo systemctl status springboot
```

### Allow Jenkins to Restart the App Without Password
```bash
echo "ubuntu ALL=(ALL) NOPASSWD: /bin/systemctl restart springboot, /bin/systemctl status springboot" \
  | sudo tee /etc/sudoers.d/springboot
```

---

## PART 6 — Create Jenkins Pipeline Job

### Step 6.1 — New Pipeline Job
1. Jenkins Dashboard → **New Item**
2. Name: `your-app-deploy`
3. Select **Pipeline** → Click **OK**

### Step 6.2 — Configure Triggers
Under **"Build Triggers"**:
- ✅ Check **"GitHub hook trigger for GITScm polling"**

### Step 6.3 — Paste Pipeline Script
Under **"Pipeline"** section → **Pipeline script**:

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven'   // Must match name set in Global Tool Config
    }

    environment {
        JAR_NAME   = 'your-app-0.0.1-SNAPSHOT.jar'   // Change to your JAR name
        DEPLOY_DIR = '/home/ubuntu'
    }

    stages {

        stage('Checkout') {
            steps {
                // Pull latest code from GitHub
                git branch: 'main',
                    url: 'https://github.com/YOUR_USERNAME/YOUR_REPO.git'
            }
        }

        stage('Build') {
            steps {
                // Build the JAR file using Maven
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Stop Old App') {
            steps {
                sh '''
                    # Stop old app gracefully, ignore error if not running
                    pkill -f "your-app-0.0.1-SNAPSHOT.jar" || true
                    sleep 3
                    echo "Old app stopped"
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    # Copy new JAR to deploy directory
                    cp target/your-app-0.0.1-SNAPSHOT.jar /home/ubuntu/your-app-0.0.1-SNAPSHOT.jar

                    # Restart the app via systemd (most reliable method)
                    sudo systemctl restart springboot
                    sleep 5

                    # Confirm app is running
                    sudo systemctl status springboot
                    echo "New app deployed successfully on port 8081"
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Deployment Successful!'
        }
        failure {
            echo '❌ Deployment Failed! Check console output.'
        }
    }
}
```

Click **Save**

---

## PART 7 — Set Up GitHub Webhook

1. Go to your GitHub repo → **Settings → Webhooks → Add webhook**
2. Fill in:
   - **Payload URL:** `http://YOUR_EC2_PUBLIC_IP:8080/github-webhook/`
   - **Content type:** `application/json`
   - **Which events:** Just the push event ✅
3. Click **Add webhook**

---

## PART 8 — Test Everything

### Manual Test (First time)
In Jenkins → Click **"Build Now"**

Watch **Console Output** — you should see:
```
✅ Checkout  → Code pulled from GitHub
✅ Build     → Maven built JAR successfully
✅ Stop      → Old app killed
✅ Deploy    → New JAR deployed via systemctl
✅ Deployment Successful!
```

### Verify App is Running
```bash
# On EC2
curl http://localhost:YOUR_APP_PORT/your-endpoint

# From browser or Postman
http://YOUR_EC2_PUBLIC_IP:YOUR_APP_PORT/your-endpoint
```

### Auto Deploy Test
```bash
# On your local machine — make any code change then:
git add .
git commit -m "test auto deploy"
git push origin main

# Watch Jenkins at: http://YOUR_EC2_PUBLIC_IP:8080
# New build should trigger automatically within seconds!
```

---

## USEFUL COMMANDS — Day to Day

```bash
# Check app status
sudo systemctl status springboot

# View app logs live
tail -f /home/ubuntu/app.log

# Restart app manually
sudo systemctl restart springboot

# Stop app
sudo systemctl stop springboot

# Check Jenkins status
sudo systemctl status jenkins

# View Jenkins logs
sudo journalctl -u jenkins -f

# Check disk space
df -h

# Check memory
free -h

# Check what's running on a port
sudo lsof -i :8081
```

---

## TROUBLESHOOTING

| Problem | Fix |
|---|---|
| Jenkins node offline (disk space) | Run: `sudo mount -o remount,size=2G /tmp` then click "Bring node online" |
| Build stuck "Waiting for executor" | Go to Manage Jenkins → Nodes → Built-In Node → "Bring this node back online" |
| App not starting after deploy | Check logs: `tail -50 /home/ubuntu/app.log` |
| Webhook not triggering Jenkins | Make sure port 8080 is open in EC2 Security Group |
| Permission denied on systemctl | Re-run the sudoers command in Part 5 |
| Out of disk space | Run: `sudo apt clean && sudo apt autoremove -y` |
| Jenkins password forgotten | Run: `cat /home/ubuntu/.jenkins/secrets/initialAdminPassword` |

---

## CHECKLIST — Before You Deploy a New App

- [ ] EC2 instance running with ports 22, 8080, and app port open
- [ ] Java 21 installed
- [ ] Maven installed
- [ ] Swap space added (2GB)
- [ ] /tmp remounted to 2GB
- [ ] Jenkins running on port 8080
- [ ] Plugins installed (SSH Agent, GitHub Integration)
- [ ] Maven configured in Jenkins Tools
- [ ] springboot.service created with correct JAR name
- [ ] sudoers rule added for ubuntu user
- [ ] Pipeline job created with correct GitHub URL and JAR name
- [ ] GitHub webhook pointing to Jenkins URL
- [ ] Security Group has all required ports open

---

*Guide created after successfully deploying Spring Boot app with full CI/CD pipeline on AWS EC2*
*Jenkins 2.555.1 | Java 21 | Maven 3.9.9 | Ubuntu 26.04*
