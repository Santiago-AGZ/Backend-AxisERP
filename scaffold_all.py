import os

# INVENTORY
base_inv = r"C:\Users\Santiago\Desktop\axisERP-platform\inventory-service\src\main\java\com\axiserp\inventory"
os.makedirs(os.path.join(base_inv, r"application\usecase"), exist_ok=True)
os.makedirs(os.path.join(base_inv, r"infrastructure\adapters\out\persistence\entity"), exist_ok=True)
os.makedirs(os.path.join(base_inv, r"infrastructure\adapters\out\persistence\repository"), exist_ok=True)
os.makedirs(os.path.join(base_inv, r"infrastructure\adapters\in\web\controller"), exist_ok=True)
with open(os.path.join(base_inv, r"application\usecase\DummyInventoryUseCase.java"), "w") as f:
    f.write("package com.axiserp.inventory.application.usecase;\nimport org.springframework.stereotype.Service;\n@Service\npublic class DummyInventoryUseCase {}")

# PURCHASE
base_pur = r"C:\Users\Santiago\Desktop\axisERP-platform\purchase-service\src\main\java\com\axiserp\purchase"
os.makedirs(os.path.join(base_pur, r"application\usecase"), exist_ok=True)
os.makedirs(os.path.join(base_pur, r"infrastructure\adapters\out\persistence\entity"), exist_ok=True)
os.makedirs(os.path.join(base_pur, r"infrastructure\adapters\out\persistence\repository"), exist_ok=True)
os.makedirs(os.path.join(base_pur, r"infrastructure\adapters\in\web\controller"), exist_ok=True)
with open(os.path.join(base_pur, r"application\usecase\DummyPurchaseUseCase.java"), "w") as f:
    f.write("package com.axiserp.purchase.application.usecase;\nimport org.springframework.stereotype.Service;\n@Service\npublic class DummyPurchaseUseCase {}")

# SALES
base_sal = r"C:\Users\Santiago\Desktop\axisERP-platform\sales-service\src\main\java\com\axiserp\sales"
os.makedirs(os.path.join(base_sal, r"application\usecase"), exist_ok=True)
os.makedirs(os.path.join(base_sal, r"infrastructure\adapters\out\persistence\entity"), exist_ok=True)
os.makedirs(os.path.join(base_sal, r"infrastructure\adapters\out\persistence\repository"), exist_ok=True)
os.makedirs(os.path.join(base_sal, r"infrastructure\adapters\in\web\controller"), exist_ok=True)
with open(os.path.join(base_sal, r"application\usecase\DummySalesUseCase.java"), "w") as f:
    f.write("package com.axiserp.sales.application.usecase;\nimport org.springframework.stereotype.Service;\n@Service\npublic class DummySalesUseCase {}")

# REPORT
base_rep = r"C:\Users\Santiago\Desktop\axisERP-platform\report-service\src\main\java\com\axiserp\report"
os.makedirs(os.path.join(base_rep, r"application\usecase"), exist_ok=True)
os.makedirs(os.path.join(base_rep, r"infrastructure\adapters\out\persistence\entity"), exist_ok=True)
os.makedirs(os.path.join(base_rep, r"infrastructure\adapters\out\persistence\repository"), exist_ok=True)
os.makedirs(os.path.join(base_rep, r"infrastructure\adapters\in\web\controller"), exist_ok=True)
with open(os.path.join(base_rep, r"application\usecase\DummyReportUseCase.java"), "w") as f:
    f.write("package com.axiserp.report.application.usecase;\nimport org.springframework.stereotype.Service;\n@Service\npublic class DummyReportUseCase {}")
