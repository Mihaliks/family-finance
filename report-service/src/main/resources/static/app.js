let token = "";
let authUrl = "http://localhost:8081";
const today = new Date().toISOString().slice(0, 10);
document.querySelector("#to").value = today;
document.querySelector("#from").value = today.slice(0, 8) + "01";

fetch("/api/ui-config").then(r => r.json()).then(c => authUrl = c.authServiceUrl);

document.querySelector("#login").onclick = async () => {
    const emailValue = document.querySelector("#email").value;
    const passwordValue = document.querySelector("#password").value;
    const response = await fetch(authUrl + "/api/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email: emailValue, password: passwordValue})
    });
    const body = await response.json();
    token = body.token || "";
    document.querySelector("#status").textContent = token ? "Вход выполнен" : (body.message || "Ошибка");
};

document.querySelector("#preset").onchange = event => {
    const now = new Date();
    let start = new Date(now);
    if (event.target.value === "MONTH") start = new Date(now.getFullYear(), now.getMonth(), 1);
    if (event.target.value === "QUARTER") start = new Date(now.getFullYear(), Math.floor(now.getMonth() / 3) * 3, 1);
    if (event.target.value === "YEAR") start = new Date(now.getFullYear(), 0, 1);
    if (event.target.value !== "CUSTOM") {
        document.querySelector("#from").value = start.toISOString().slice(0, 10);
        document.querySelector("#to").value = today;
    }
};

function query() {
    const p = new URLSearchParams({
        from: document.querySelector("#from").value,
        to: document.querySelector("#to").value,
        groupBy: document.querySelector("#groupBy").value
    });
    const family = document.querySelector("#familyId").value;
    const members = document.querySelector("#memberIds").value;
    const operationType = document.querySelector("#type").value;
    if (family) p.set("familyId", family);
    if (members) p.set("memberIds", members);
    if (operationType) p.set("type", operationType);
    return p;
}

document.querySelector("#load").onclick = async () => {
    const response = await fetch("/api/reports?" + query(), {headers: {Authorization: "Bearer " + token}});
    document.querySelector("#result").textContent = JSON.stringify(await response.json(), null, 2);
};

document.querySelector("#csv").onclick = async () => {
    const response = await fetch("/api/reports/export.csv?" + query(), {headers: {Authorization: "Bearer " + token}});
    const link = document.createElement("a");
    link.href = URL.createObjectURL(await response.blob());
    link.download = "report.csv";
    link.click();
};
