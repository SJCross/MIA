clear

pnameIn = 'C:\Users\sc13967\Documents\Java Projects\Common\src\test\resources\images\';
fnameIn = 'BinaryCircle2D_8bit_whiteBG.tif';

pnameOut = 'C:\Users\sc13967\Documents\Java Projects\Common\src\test\resources\coordinates\';
fnameOut = 'BinaryCircle2D_8bit_whiteBG.csv';

nZ = 1;
nT = 1;

for j=1:nT
    for i = 1:nZ
        im(:,:,i,j) = imread([pnameIn,fnameIn],(j-1)*nZ+i);
    end
end

[x,y,z,t] = ind2sub(size(im),find(im==0));
coords = zeros(numel(x),7);
for i=1:numel(x)
   newCoord = [im(x(i),y(i),z(i),t(i)),im(x(i),y(i),z(i)),y(i)-1,x(i)-1,0,z(i)-1,t(i)-1];
   coords(i,:) = newCoord;
    
end

csvwrite([pnameOut,fnameOut],coords);