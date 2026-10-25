const fs=require('fs');
const p='C:/Users/ronad/Desktop/SaveFrut/backend/ms-ordenes/src/main/java/com/savefrut/msordenes/entity/Orden.java';
let s=fs.readFileSync(p,'utf8');
s=s.replace(/@ManyToOne[\s\S]*?@JoinColumn\(name = "cliente_id"[\s\S]*?private [^\n]+;/, '    @Column(name = "cliente_id", nullable = false)\n    private Long clienteId;');
fs.writeFileSync(p,s);
console.log('ok');
