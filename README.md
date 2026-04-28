## What is Packer?

**Packer** is an open-source tool created by HashiCorp that automates the creation of identical machine images across multiple platforms from a single configuration file .

Think of it as a "photo-booth for servers" — you define what software and settings you want on your server (like installing Nginx or copying configuration files), and Packer automatically launches a temporary EC2 instance, applies those customizations, and then saves a reusable AMI. Once the image is created, it terminates the temporary instance, leaving you with a ready-to-use, consistent, and version-controlled AMI.

**Why use Packer?**
- **Automation** – No more manual clicking through the AWS Console to create AMIs.
- **Consistency** – Every image is built from the exact same code, eliminating configuration drift.
- **CI/CD Integration** – Run `packer build` in your pipelines to automatically create fresh images on code changes.
- **Multi-Platform** – The same template can also build images for Docker, VMware, or Google Cloud .

---

## How Packer Works with AWS AMI

Here’s the step-by-step workflow :

1. **Define a Template** – You write a configuration file (in HCL2 format) that specifies:
   - The **base AMI** to start from (e.g., an official Ubuntu or Amazon Linux image).
   - The **instance type** for the temporary builder (e.g., `t2.micro`).
   - **Provisioning steps** – Shell scripts or commands to install software (e.g., `apt-get install nginx`).
2. **Launch Temporary Instance** – Packer automatically spins up an EC2 instance using your base image.
3. **Provision** – Packer connects via SSH or WinRM and runs all your installation/configuration commands.
4. **Create AMI** – After provisioning, Packer snapshots the instance into a new, custom AMI.
5. **Clean Up** – Packer terminates the temporary EC2 instance.
6. **Output** – Your new AMI ID is displayed and ready to launch.

---

## Step-by-Step: Creating a Custom AWS AMI with Packer

We'll use **HCL2**, the modern Packer configuration language (JSON is being deprecated) .

### Step 1: Prerequisites

- **AWS Account** with permissions to launch EC2 instances and create AMIs.
- **AWS CLI configured** (run `aws configure`) or environment variables set:
  ```bash
  export AWS_ACCESS_KEY_ID="your-access-key"
  export AWS_SECRET_ACCESS_KEY="your-secret-key"
  export AWS_REGION="us-east-1"
  ```
- **Packer installed** on your local machine or build server .

#### Install Packer

**macOS:**
```bash
brew install packer
```

**Linux:**
```bash
curl -LO https://releases.hashicorp.com/packer/1.11.2/packer_1.11.2_linux_amd64.zip
unzip packer_1.11.2_linux_amd64.zip
sudo mv packer /usr/local/bin/
```

**Verify installation:**
```bash
packer --version
```

---

### Step 2: Create a Packer Template

Create a new file named `ami.pkr.hcl`. This is your Packer configuration.

Here's a complete template that builds an **Amazon Linux 2023** AMI with **Apache HTTP Server** installed :

```hcl
# Required plugins
packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# Variable definitions
variable "ami_prefix" {
  type    = string
  default = "my-custom-ami"
}

variable "instance_type" {
  type    = string
  default = "t2.micro"
}

variable "region" {
  type    = string
  default = "us-east-1"
}

# Source block - defines the base image and builder settings
source "amazon-ebs" "amazon-linux" {
  ami_name      = "${var.ami_prefix}-${formatdate("YYYYMMDDhhmmss", timestamp())}"
  instance_type = var.instance_type
  region        = var.region
  
  # Filter to find the latest Amazon Linux 2023 AMI
  source_ami_filter {
    filters = {
      name                = "al2023-ami-2023.*-x86_64"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["amazon"]
  }
  
  ssh_username = "ec2-user"
  
  # Optional: Add tags to the AMI
  tags = {
    Name        = "Custom Apache AMI"
    BuiltBy     = "Packer"
    Environment = "Development"
  }
}

# Build block - defines what to do with the source
build {
  sources = ["source.amazon-ebs.amazon-linux"]

  # Shell provisioner - runs commands to install software
  provisioner "shell" {
    inline = [
      "sudo yum update -y",
      "sudo yum install -y httpd",
      "sudo systemctl enable httpd",
      "sudo systemctl start httpd",
      "echo '<html><body><h1>Hello from Packer AMI!</h1></body></html>' | sudo tee /var/www/html/index.html"
    ]
  }
}
```

#### Key Configuration Details:

| Parameter | Description |
|-----------|-------------|
| `source_ami_filter` | Dynamically finds the latest official Amazon Linux 2023 AMI  |
| `ami_name` | Uses timestamp to create unique names for each build |
| `ssh_username` | Must match the OS (`ec2-user` for Amazon Linux, `ubuntu` for Ubuntu) |
| `provisioner "shell"` | Commands to run inside the temporary instance. You can also run entire scripts using `scripts = ["install.sh"]` |

---

### Step 3: Initialize, Validate, and Build

#### Initialize (downloads the AWS plugin)
```bash
packer init .
```

#### Format and Validate (check for syntax errors)
```bash
packer fmt .
packer validate ami.pkr.hcl
```

**Expected output:** `The configuration is valid.`

#### Build the AMI
```bash
packer build ami.pkr.hcl
```

**What you'll see during the build** :
```
==> amazon-ebs.amazon-linux: Prevalidating AMI Name: my-custom-ami-20250702143408
==> amazon-ebs.amazon-linux: Launching a source AWS instance...
    amazon-ebs.amazon-linux: Instance ID: i-0dd7b3c87f1785e55
==> amazon-ebs.amazon-linux: Waiting for instance to become ready...
==> amazon-ebs.amazon-linux: Connected to SSH!
==> amazon-ebs.amazon-linux: Provisioning with shell script: inline
    amazon-ebs.amazon-linux: Installing: httpd
==> amazon-ebs.amazon-linux: Creating AMI from instance i-0dd7b3c87f1785e55
==> amazon-ebs.amazon-linux: AMI: ami-0abc123def456...
==> amazon-ebs.amazon-linux: Cleaning up temporary resources...
Build 'amazon-ebs.amazon-linux' finished.

==> Builds finished. The artifacts of successful builds are:
--> amazon-ebs.amazon-linux: AMIs were created:
us-east-1: ami-0abc123def456...
```

**Copy the AMI ID** (`ami-xxxxxxxxx`) that appears at the end – you'll use it to launch EC2 instances.

---

### Step 4: Verify Your Custom AMI

1. Open **AWS Console** → **EC2** → **AMIs** (under "Images" in the left menu).
2. Change the filter from "Owned by me" to **"Private images"** or search by your AMI name.
3. You should see your newly created AMI with status **"available"** .

---

### Step 5: Launch an EC2 Instance from Your Custom AMI

1. Select your AMI, click **"Launch instance from AMI"**.
2. Choose an instance type (e.g., `t2.micro`).
3. Configure security group (ensure port 80/443 is open if you installed a web server).
4. Launch the instance.
5. Once running, access its **public IP** in a browser – you should see `Hello from Packer AMI!`.

---

## Advanced: Using Shell Scripts Instead of Inline Commands

For complex installations, save commands to a separate file .

**Create `install_apache.sh`**:
```bash
#!/bin/bash
sudo yum update -y
sudo yum install -y httpd
sudo systemctl enable httpd
sudo systemctl start httpd

# Fetch instance metadata
TOKEN=$(curl --request PUT "http://169.254.169.254/latest/api/token" --header "X-aws-ec2-metadata-token-ttl-seconds:3600")
AMI_ID=$(curl -s http://169.254.169.254/latest/meta-data/ami-id --header "X-aws-ec2-metadata-token: $TOKEN")

cat <<EOF | sudo tee /var/www/html/index.html
<html><body><h1>Built from AMI: $AMI_ID</h1></body></html>
EOF
```

**Update your Packer template**:
```hcl
provisioner "shell" {
  script = "install_apache.sh"
}
```

---

## Integrating Packer into CI/CD (GitHub Actions Example)

Automate your AMI builds whenever you push code changes .

**Create `.github/workflows/packer.yml`**:
```yaml
name: Build Custom AMI

on:
  push:
    branches: [main]

jobs:
  build-ami:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      
      - name: Install Packer
        run: |
          curl -LO https://releases.hashicorp.com/packer/1.11.2/packer_1.11.2_linux_amd64.zip
          unzip packer_1.11.2_linux_amd64.zip
          sudo mv packer /usr/local/bin/
      
      - name: Packer Build
        run: |
          packer init .
          packer build ami.pkr.hcl
```

> **Security Tip:** Store AWS credentials as GitHub Secrets (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`) .

---

## Quick Reference: Common Packer Commands

| Command | Purpose |
|---------|---------|
| `packer init .` | Download required plugins |
| `packer fmt .` | Format template files |
| `packer validate file.pkr.hcl` | Check for syntax errors |
| `packer build file.pkr.hcl` | Build the AMI |
| `packer hcl2_upgrade legacy.json` | Convert JSON to HCL2  |

---

## Troubleshooting Tips

| Issue | Solution |
|-------|----------|
| **"Source AMI not found"** | Verify the `source_ami_filter` name pattern matches an official AMI for your region |
| **SSH connection timeout** | Ensure the `ssh_username` matches the OS (`ec2-user` for Amazon Linux, `ubuntu` for Ubuntu) |
| **Permissions error** | Your IAM user needs `EC2FullAccess` and `S3FullAccess` policies |
| **Build very slow** | Use a smaller instance type like `t2.micro` if available |

---

## Next Steps

- Try building images for **Ubuntu** (change `source_ami_filter` and `ssh_username` to `ubuntu`)
- Add **multiple provisioners** (file uploads + shell scripts) 
- Share your AMI across **multiple AWS accounts** 
- Build a **"golden image"** pipeline with automated testing baked in

Would you like me to provide a template for Ubuntu, Windows, or a specific software stack (like Node.js, Python, or Docker)?# demopro
