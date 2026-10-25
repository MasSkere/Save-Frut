const fs=require('fs');
const p='C:/Users/ronad/Desktop/SaveFrut/backend/ms-ordenes/src/main/java/com/savefrut/msordenes/entity/DetalleOrden.java';
let s=fs.readFileSync(p,'utf8');
s=s.replace(/@ManyToOne[\s\S]*?@JoinColumn\(name = "producto_id"[\s\S]*?private [^\n]+;/, '    @Column(name = "producto_id", nullable = false)\n    private Long productoId;');
fs.writeFileSync(p,s);
console.log('ok');
