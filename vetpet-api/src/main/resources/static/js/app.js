document.addEventListener("DOMContentLoaded", () => {
    const lateral = document.querySelector(".lateral");
    document.querySelectorAll("[data-menu-toggle]").forEach((botao) => {
        botao.addEventListener("click", () => {
            const aberta = lateral.classList.toggle("aberta");
            botao.setAttribute("aria-expanded", String(aberta));
        });
    });

    document.addEventListener("keydown", (evento) => {
        if (evento.key === "Escape" && lateral) {
            lateral.classList.remove("aberta");
        }
    });

    document.querySelectorAll("form[data-confirmar]").forEach((formulario) => {
        formulario.addEventListener("submit", (evento) => {
            if (!window.confirm(formulario.dataset.confirmar)) {
                evento.preventDefault();
            }
        });
    });

    document.querySelectorAll("[data-fechar-alerta]").forEach((botao) => {
        botao.addEventListener("click", () => botao.closest(".alerta").remove());
    });

    document.querySelectorAll("[data-abas]").forEach((grupo) => {
        const botoes = grupo.querySelectorAll("[role='tab']");
        const ativar = (botao) => {
            botoes.forEach((outro) => {
                const selecionado = outro === botao;
                outro.setAttribute("aria-selected", String(selecionado));
                outro.tabIndex = selecionado ? 0 : -1;
                document.getElementById(outro.getAttribute("aria-controls")).hidden = !selecionado;
            });
        };
        botoes.forEach((botao, indice) => {
            botao.addEventListener("click", () => {
                ativar(botao);
                history.replaceState(null, "", "#" + botao.getAttribute("aria-controls"));
            });
            botao.addEventListener("keydown", (evento) => {
                const direcao = evento.key === "ArrowRight" ? 1 : evento.key === "ArrowLeft" ? -1 : 0;
                if (direcao !== 0) {
                    const proximo = botoes[(indice + direcao + botoes.length) % botoes.length];
                    proximo.focus();
                    ativar(proximo);
                }
            });
        });
        const inicial = Array.from(botoes).find((botao) => "#" + botao.getAttribute("aria-controls") === location.hash);
        ativar(inicial || botoes[0]);
    });
});
