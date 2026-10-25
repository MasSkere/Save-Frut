const fs=require('fs');
const p=require('path');
function w(d){
  fs.readdirSync(d).forEach(e=>{
    const f=p.join(d,e);
    const s=fs.statSync(f);
    if(s.isDirectory()) w(f);
    else if(f.endsWith('.java')){
      let c=fs.readFileSync(f,'utf8');
      let n=c.split('com.savefrut.api.catalogo').join('com.savefrut.mscatalogo');
      n=n.split('com.savefrut.api.usuarios').join('com.savefrut.msidentidad');
      n=n.split('com.savefrut.api.common').join('com.savefrut.mscatalogo.common');
      if(n!==c){ fs.writeFileSync(f,n); }
    }
  });
}
w('C:/Users/ronad/Desktop/SaveFrut/backend/ms-catalogo/src');
console.log('done');
