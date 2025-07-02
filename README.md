# PROCESS MANAGER

![Process Manager](ProcessManager.gif)

El patrón **Process Manager** es un patrón de arquitectura de integración empresarial (Enterprise Integration Pattern) que se utiliza para coordinar y controlar el flujo de un proceso de negocio complejo que involucra múltiples pasos o servicios.

Un Process Manager es un componente que mantiene el estado de un proceso de negocio a largo plazo, coordina las interacciones entre varios servicios o tareas que lo componen y decide qué acción ejecutar a continuación en función de los eventos o resultados anteriores.

---

##  ¿Qué hace un Process Manager?

- Actúa como orquestador del flujo del proceso.
- Guarda el estado del proceso entre pasos (por ejemplo, en una base de datos o en memoria).
- Recibe eventos o mensajes que indican el progreso de ciertas tareas.
- Decide qué pasos ejecutar a continuación según una lógica definida.
- Puede enviar comandos o mensajes a otros servicios para continuar el flujo.

---

##  Ejemplo de uso - Proceso de compra

1. El usuario realiza un pedido.
2. El Process Manager de compra:
    - Envía comando para reservar inventario.
    - Espera confirmación.
    - Luego envía comando para procesar el pago.
    - Si falla, revierte el inventario.
    - Finalmente, genera orden de envío.