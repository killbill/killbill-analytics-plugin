# KillBill Analytics Reports Setup Script

This script installs all necessary database DDLs and creates KillBill analytics reports for your KillBill environment.

---

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Usage](#usage)
- [Environment Variables](#environment-variables)
- [Script Behavior](#script-behavior)
- [Examples](#examples)
- [License](#license)

---

## Overview

This Bash script performs the following tasks:

1. Installs database DDL files (`.sql` or `.ddl`) into the configured MySQL database.
2. Creates KillBill analytics reports via the KillBill Analytics plugin REST API.
3. Supports optional dropping of existing reports before creation.

The script recursively installs DDL files, ensuring `v_report_*.ddl` files are installed before corresponding `report_*.ddl` files. If no `v_report_*.ddl` exists in a folder, all `.ddl` files in that folder are installed.

---

## Prerequisites

- **Bash shell** (Linux, macOS, or Windows Git Bash)
- **MySQL client** installed and accessible in PATH
- KillBill server running with the KillBill Analytics plugin installed
- Appropriate permissions for the MySQL database and KillBill API

---

## Usage

Run the script from the directory containing your DDL files:

```bash
./setup_reports.sh
```

By default, the script installs DDLs and creates all reports.

---

## Environment Variables

The script uses environment variables to configure MySQL and KillBill settings. Defaults are provided if variables are not set:

| Variable                 | Default                  | Description                                                               |
|--------------------------|--------------------------|---------------------------------------------------------------------------|
| `KILLBILL_HTTP_PROTOCOL` | `http`                   | KillBill API protocol                                                     |
| `KILLBILL_HOST`          | `127.0.0.1`              | KillBill host                                                             |
| `KILLBILL_PORT`          | `8080`                   | KillBill port                                                             |
| `KILLBILL_USER`          | `admin`                  | KillBill username                                                         |
| `KILLBILL_PASSWORD`      | `password`               | KillBill password                                                         |
| `KILLBILL_API_KEY`       | `bob`                    | KillBill API key                                                          |
| `KILLBILL_API_SECRET`    | `lazar`                  | KillBill API secret                                                       |
| `MYSQL_HOST`             | `127.0.0.1`              | MySQL host                                                                |
| `MYSQL_USER`             | `root`                   | MySQL user                                                                |
| `MYSQL_PASSWORD`         | `killbill`               | MySQL password                                                            |
| `MYSQL_DATABASE`         | `killbill`               | MySQL database name                                                       |
| `INSTALL_DDL`            | `true`                   | Whether to install DDL files (`true` or `false`)                          |
| `DROP_EXISTING_REPORT`   | `true`                   | Whether to drop existing reports before creating them (`true` or `false`) |

You can export environment variables before running the script to override defaults:

```bash
export KILLBILL_HOST=192.168.1.10
export MYSQL_PASSWORD=mysecret
export INSTALL_DDL=false

./setup_reports.sh
```

---

## Script Behavior

1. **DDL Installation**
    - Installs ddl from the `utils` directory first.
    - Recursively installs DDLs from the other subdirectories:
        - If `v_report_*.ddl` files exist, they are installed first, followed by `report_*.ddl`.
        - If no `v_report_*.ddl` exists, all `.ddl` files in the folder are installed.

2. **Report Creation**
    - All reports defined in the `create_all_reports` function are created.
    - If `DROP_EXISTING_REPORT=true`, existing reports are deleted before creation.
    - Reports are created via the KillBill Analytics plugin REST API.

---

## Examples

- **Run with default configuration:**

```bash
./setup_reports.sh
```

- **Skip DDL installation:**

```bash
export INSTALL_DDL=false
./setup_reports.sh
```

- **Disable dropping existing reports:**

```bash
export DROP_EXISTING_REPORT=false
./setup_reports.sh
```

- **Override KillBill host and MySQL password:**

```bash
export KILLBILL_HOST=192.168.1.10
export MYSQL_PASSWORD=mysecret
./setup_reports.sh
```

---

## License

This script is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).